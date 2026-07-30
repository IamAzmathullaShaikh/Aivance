package com.bangersoul.aivance.sdk.core

/**
 * Metadata describing an AI provider.
 *
 * @property id Unique identifier for the provider.
 * @property name Human-readable name of the provider.
 * @property version Version string of the provider implementation.
 * @property description Brief description of what the provider does.
 * @property icon Optional icon identifier or URI.
 * @property author The entity or individual who created the provider.
 */
data class ProviderMetadata(
    val id: String,
    val name: String,
    val version: String,
    val description: String,
    val icon: String? = null,
    val author: String
)
