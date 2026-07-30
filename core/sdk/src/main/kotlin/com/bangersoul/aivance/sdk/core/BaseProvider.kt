package com.bangersoul.aivance.sdk.core

import java.util.concurrent.atomic.AtomicReference

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
    private val _status = AtomicReference(ProviderStatus.Uninitialized)
    
    /**
     * Current lifecycle status of the provider.
     */
    val status: ProviderStatus
        get() = _status.get()

    /**
     * Updates the provider status in a thread-safe manner.
     * 
     * @param newStatus The new status to transition to.
     */
    fun updateStatus(newStatus: ProviderStatus) {
        _status.set(newStatus)
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
