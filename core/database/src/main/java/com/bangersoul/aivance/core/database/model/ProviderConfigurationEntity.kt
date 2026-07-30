package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "provider_configurations")
data class ProviderConfigurationEntity(
    @PrimaryKey
    val provider: String,
    val apiKey: String,
    val baseUrl: String?,
    val settings: Map<String, String>
)
