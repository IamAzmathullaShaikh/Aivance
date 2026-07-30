package com.bangersoul.aivance.sdk.infrastructure

import com.bangersoul.aivance.sdk.core.BaseProvider
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for creating [BaseProvider] instances.
 *
 * Supports a plug-and-play architecture by allowing registration of custom provider factories.
 */
@Singleton
class ProviderFactory @Inject constructor(
    private val injectedFactories: Map<String, @JvmSuppressWildcards Factory>
) {

    /**
     * Interface for individual provider factories.
     */
    fun interface Factory {
        /**
         * Creates a new instance of a provider.
         *
         * @param config Optional configuration parameters for the provider.
         * @return A new instance of [BaseProvider].
         */
        fun create(config: Map<String, Any>?): BaseProvider
    }

    private val customFactories = ConcurrentHashMap<String, Factory>()

    /**
     * Registers a factory for a specific provider type.
     *
     * @param type The unique type identifier for the provider (e.g., "openai", "gemini").
     * @param factory The factory implementation responsible for creating instances.
     */
    fun registerFactory(type: String, factory: Factory) {
        customFactories[type] = factory
    }

    /**
     * Unregisters a factory for a specific provider type.
     *
     * @param type The type identifier to unregister.
     */
    fun unregisterFactory(type: String) {
        customFactories.remove(type)
    }

    /**
     * Creates a provider instance for the given type and configuration.
     *
     * @param type The type identifier of the provider to create.
     * @param config Optional configuration for the provider instance.
     * @return A new [BaseProvider] instance.
     * @throws IllegalArgumentException if no factory is registered for the given type.
     */
    fun createProvider(type: String, config: Map<String, Any>? = null): BaseProvider {
        val factory = customFactories[type] ?: injectedFactories[type]
            ?: throw IllegalArgumentException("No factory registered for provider type: $type")
        return factory.create(config)
    }

    /**
     * Checks if a factory is registered for a specific provider type.
     *
     * @param type The type identifier to check.
     * @return True if a factory exists, false otherwise.
     */
    fun hasFactory(type: String): Boolean {
        return customFactories.containsKey(type) || injectedFactories.containsKey(type)
    }
}
