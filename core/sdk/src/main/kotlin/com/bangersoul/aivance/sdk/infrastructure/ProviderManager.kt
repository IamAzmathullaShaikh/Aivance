package com.bangersoul.aivance.sdk.infrastructure

import com.bangersoul.aivance.sdk.core.BaseProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderStatus
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

    /**
     * Initializes all currently registered providers.
     * Moves providers from [ProviderStatus.Uninitialized] to [ProviderStatus.Ready].
     */
    suspend fun initializeAll() {
        registry.getAllProviders().forEach { provider ->
            orchestrator.transitionTo(provider, ProviderStatus.Ready)
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
