package com.bangersoul.aivance.sdk.infrastructure

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.BaseProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the lifecycle and orchestration of all registered providers.
 *
 * This manager ensures that providers transition through their lifecycle states
 * in a controlled and thread-safe manner.
 */
@Singleton
class ProviderManager @Inject constructor(
    private val registry: ProviderRegistry
) {
    private val orchestrator = LifecycleOrchestrator()

    private val _providerStatuses = MutableStateFlow<Map<String, ProviderStatus>>(emptyMap())

    /**
     * Reactive map of all registered providers and their current status.
     */
    val providerStatuses: StateFlow<Map<String, ProviderStatus>> = _providerStatuses.asStateFlow()

    init {
        // Initialize status map
        val initialMap = mutableMapOf<String, ProviderStatus>()
        registry.getAllProviders().forEach { provider ->
            initialMap[provider.metadata.id] = provider.status
        }
        _providerStatuses.value = initialMap
    }

    /**
     * Initializes all currently registered providers.
     * Moves providers from [ProviderStatus.Uninitialized] to [ProviderStatus.Ready].
     */
    suspend fun initializeAll() {
        registry.getAllProviders().forEach { provider ->
            orchestrator.transitionTo(provider, ProviderStatus.Ready)
            updateInternalStatus(provider.metadata.id, provider.status)
        }
    }

    /**
     * Starts a specific provider by its ID.
     * Moves the provider to [ProviderStatus.Active], ensuring it is initialized first if necessary.
     *
     * @param id The unique identifier of the provider to start.
     */
    suspend fun startProvider(id: String) {
        registry.getProvider(id)?.let { provider ->
            orchestrator.transitionTo(provider, ProviderStatus.Active)
            updateInternalStatus(id, provider.status)
        }
    }

    /**
     * Stops a specific provider by its ID.
     * Moves the provider from [ProviderStatus.Active] back to [ProviderStatus.Ready].
     *
     * @param id The unique identifier of the provider to stop.
     */
    suspend fun stopProvider(id: String) {
        registry.getProvider(id)?.let { provider ->
            orchestrator.transitionTo(provider, ProviderStatus.Ready)
            updateInternalStatus(id, provider.status)
        }
    }

    /**
     * Validates a provider configuration without persisting it.
     * Useful during onboarding or settings updates.
     */
    suspend fun validateProvider(id: String, config: ProviderConfiguration): Result<Unit> {
        val provider = registry.getProvider(id) ?: return Result.Failure(
            com.bangersoul.aivance.core.common.result.DomainError("Provider $id not found")
        )

        return try {
            updateInternalStatus(id, ProviderStatus.Initializing)

            // In a real implementation, we would apply the config to the provider instance
            // and perform a 'ping' test.
            provider.onInitialize()
            val health = provider.checkHealth()

            updateInternalStatus(id, health)

            if (health == ProviderStatus.Ready || health == ProviderStatus.Active) {
                Result.Success(Unit)
            } else {
                Result.Failure(com.bangersoul.aivance.core.common.result.ProviderError(
                    providerId = id, message = "Validation failed with status: $health"
                ))
            }
        } catch (e: Exception) {
            updateInternalStatus(id, ProviderStatus.Error)
            Result.Failure(com.bangersoul.aivance.core.common.result.ProviderError(
                providerId = id, message = e.message ?: "Validation failed", cause = e
            ))
        }
    }

    /**
     * Retrieves the best available provider for a given capability.
     *
     * @param capability The capability required.
     * @return The best provider matching the capability, or null if none are available.
     */
    fun getBestProviderFor(capability: ProviderCapability): BaseProvider? {
        val candidates = registry.getProvidersByCapability(capability)

        // Priority: Active > Ready > Others
        return candidates.firstOrNull { it.status == ProviderStatus.Active }
            ?: candidates.firstOrNull { it.status == ProviderStatus.Ready }
            ?: candidates.firstOrNull()
    }

    /**
     * Triggers a health check for a specific provider.
     *
     * @param id The unique identifier of the provider.
     */
    fun triggerHealthCheck(id: String) {
        // Placeholder for health check logic
    }

    private fun updateInternalStatus(id: String, status: ProviderStatus) {
        _providerStatuses.update { current ->
            current.toMutableMap().apply { put(id, status) }
        }
    }

    /**
     * Internal orchestrator that manages state transitions for providers.
     */
    private class LifecycleOrchestrator {
        private val providerLocks = ConcurrentHashMap<String, Mutex>()

        /**
         * Transitions a provider to the target status, ensuring all intermediate steps are taken.
         *
         * @param provider The provider to transition.
         * @param targetStatus The desired status.
         */
        suspend fun transitionTo(provider: BaseProvider, targetStatus: ProviderStatus) {
            val mutex = providerLocks.getOrPut(provider.metadata.id) { Mutex() }
            mutex.withLock {
                executeTransition(provider, targetStatus)
            }
        }

        private suspend fun executeTransition(provider: BaseProvider, targetStatus: ProviderStatus) {
            var currentStatus = provider.status
            if (currentStatus == targetStatus) return

            // Ensure Uninitialized -> Ready -> Active order
            when (targetStatus) {
                ProviderStatus.Ready -> {
                    if (currentStatus == ProviderStatus.Uninitialized) {
                        performInitialize(provider)
                    } else if (currentStatus == ProviderStatus.Active) {
                        performStop(provider)
                    }
                }
                ProviderStatus.Active -> {
                    if (currentStatus == ProviderStatus.Uninitialized) {
                        performInitialize(provider)
                        currentStatus = provider.status
                    }
                    if (currentStatus == ProviderStatus.Ready) {
                        performStart(provider)
                    }
                }
                ProviderStatus.Disposed -> {
                    performDispose(provider)
                }
                else -> {}
            }
        }

        private suspend fun performInitialize(provider: BaseProvider) {
            provider.updateStatus(ProviderStatus.Initializing)
            try {
                provider.onInitialize()
                provider.updateStatus(ProviderStatus.Ready)
            } catch (ignored: Exception) {
                provider.updateStatus(ProviderStatus.Error)
            }
        }

        private suspend fun performStart(provider: BaseProvider) {
            try {
                provider.onStart()
                provider.updateStatus(ProviderStatus.Active)
            } catch (ignored: Exception) {
                provider.updateStatus(ProviderStatus.Error)
            }
        }

        private suspend fun performStop(provider: BaseProvider) {
            try {
                provider.onStop()
                provider.updateStatus(ProviderStatus.Ready)
            } catch (ignored: Exception) {
                provider.updateStatus(ProviderStatus.Error)
            }
        }

        private suspend fun performDispose(provider: BaseProvider) {
            try {
                provider.onDispose()
                provider.updateStatus(ProviderStatus.Disposed)
            } catch (ignored: Exception) {
                provider.updateStatus(ProviderStatus.Error)
            }
        }
    }
}
