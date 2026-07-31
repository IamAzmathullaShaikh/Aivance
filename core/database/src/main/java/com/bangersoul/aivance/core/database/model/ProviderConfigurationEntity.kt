package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "provider_configurations")
data class ProviderConfigurationEntity(
    @PrimaryKey
    val provider: String,
    val type: String, // "AI", "JOB", "ENRICHMENT"
    val baseUrl: String? = null,
    val selectedModel: String? = null,
    val actorId: String? = null,
    val settings: Map<String, String> = emptyMap(),
    val isEnabled: Boolean = true
)
