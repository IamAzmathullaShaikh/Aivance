package com.bangersoul.aivance.sdk.infrastructure

import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.api.JobProvider
import com.bangersoul.aivance.sdk.core.BaseProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registry for managing providers (AI, Job, etc.) in the SDK.
 *
 * Provides thread-safe methods to register, unregister, and query providers.
 * Automatically registers multibound providers on initialization.
 */
@Singleton
class ProviderRegistry @Inject constructor(
    aiProviders: Set<@JvmSuppressWildcards AIProvider>,
    jobProviders: Set<@JvmSuppressWildcards JobProvider>
) {

    private val providers = ConcurrentHashMap<String, BaseProvider>()

    init {
        aiProviders.forEach { register(it) }
        jobProviders.forEach { register(it) }
    }

    /**
     * Registers a provider in the registry.
     * If a provider with the same ID already exists, it will be overwritten.
     *
     * @param provider The provider to register.
     */
    fun register(provider: BaseProvider) {
        providers[provider.metadata.id] = provider
    }

    /**
     * Unregisters a provider from the registry by its ID.
     *
     * @param id The unique identifier of the provider to unregister.
     */
    fun unregister(id: String) {
        providers.remove(id)
    }

    /**
     * Retrieves a provider by its unique identifier.
     *
     * @param id The unique identifier of the provider.
     * @return The provider if found, null otherwise.
     */
    fun getProvider(id: String): BaseProvider? {
        return providers[id]
    }

    /**
     * Retrieves all providers that support a specific capability.
     *
     * @param capability The capability to filter providers by.
     * @return A list of providers that support the given capability.
     */
    fun getProvidersByCapability(capability: ProviderCapability): List<BaseProvider> {
        return providers.values.filter { it.hasCapability(capability) }
    }

    /**
     * Retrieves all currently registered providers.
     *
     * @return A list of all registered providers.
     */
    fun getAllProviders(): List<BaseProvider> {
        return providers.values.toList()
    }
}
