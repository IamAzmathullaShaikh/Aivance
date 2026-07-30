package com.bangersoul.aivance.sdk.config

/**
 * Data class representing the configuration for a provider.
 *
 * @property providerId The unique identifier for the provider.
 * @property settings A map of non-sensitive configuration settings.
 * @property secrets A map of sensitive configuration settings (secrets). 
 *                 Values should be stored as encrypted placeholders or ciphertexts.
 */
data class ProviderConfiguration(
    val providerId: String,
    val settings: Map<String, String> = emptyMap(),
    val secrets: Map<String, String> = emptyMap()
)
