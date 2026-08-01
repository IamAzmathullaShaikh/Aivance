package com.bangersoul.aivance.sdk.core

import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Abstract base class for all AI providers.
 *
 * This class manages the provider's metadata, status, and capabilities,
 * providing a consistent lifecycle and thread-safe status updates.
 *
 * @property metadata Information about the provider.
 * @property capabilities The set of capabilities supported by this provider.
 */
abstract class BaseProvider(
    val metadata: ProviderMetadata,
    private val capabilities: Set<ProviderCapability>
) {
    private val _status = MutableStateFlow(ProviderStatus.Uninitialized)

    /**
     * Current lifecycle status of the provider.
     */
    val status: ProviderStatus
        get() = _status.value

    /**
     * Observable stream of status changes.
     */
    val statusFlow: StateFlow<ProviderStatus> = _status.asStateFlow()

    /**
     * Updates the provider status in a thread-safe manner.
     *
     * @param newStatus The new status to transition to.
     */
    fun updateStatus(newStatus: ProviderStatus) {
        _status.value = newStatus
    }

    /**
     * Checks if the provider supports a specific capability.
     *
     * @param capability The capability to check for.
     * @return True if supported, false otherwise.
     */
    fun hasCapability(capability: ProviderCapability): Boolean {
        return capabilities.contains(capability)
    }

    /**
     * Whether this provider currently holds usable credentials/configuration.
     *
     * Used by [com.bangersoul.aivance.sdk.infrastructure.ProviderManager.getBestProviderFor]
     * to prefer configured providers (e.g. a Groq instance with a real API key)
     * over unconfigured ones that merely self-mark Ready.
     */
    open val isConfigured: Boolean
        get() = true

    /**
     * Whether this provider currently holds real user-supplied credentials.
     *
     * This is stronger than [isConfigured]: keyless providers (e.g. Ollama, which
     * needs no API key) report [isConfigured] == true even though they hold no
     * secrets and may point at an unreachable local server. [getBestProviderFor]
     * prefers providers with real credentials so a keyed Groq/OpenAI/Claude wins
     * over a keyless Ollama, regardless of registry iteration order.
     */
    open val hasCredentials: Boolean
        get() = false

    /**
     * Applies a new configuration to this provider instance at runtime.
     *
     * Enables the "reconfigure" flow: after a user validates/saves credentials
     * (onboarding, AI settings, provider management), the live DI-singleton
     * provider picks up the new key without an app restart.
     *
     * Default implementation is a no-op; credential-based providers override it.
     *
     * @param config The new configuration to apply.
     */
    open suspend fun applyConfiguration(config: ProviderConfiguration) {
        // No-op by default.
    }

    /**
     * Checks the health of the provider.
     * Default implementation returns current status.
     * @return The updated status after the check.
     */
    open suspend fun checkHealth(): ProviderStatus {
        return status
    }

    /**
     * Lifecycle hook called when the provider is being initialized.
     * Implementation should handle any one-time setup required.
     */
    abstract suspend fun onInitialize()

    /**
     * Lifecycle hook called when the provider is starting up.
     * Implementation should prepare for active use.
     */
    abstract suspend fun onStart()

    /**
     * Lifecycle hook called when the provider is stopping.
     * Implementation should pause or cease operations.
     */
    abstract suspend fun onStop()

    /**
     * Lifecycle hook called when the provider is being disposed of.
     * Implementation should release all resources.
     */
    abstract suspend fun onDispose()
}
