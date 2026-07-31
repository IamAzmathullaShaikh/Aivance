package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class CompanyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val domain: String?,
    val logoUrl: String?,
    val website: String?,
    val industry: String?,
    val headquarters: String?,
    val socialLinks: Map<String, String>? = emptyMap()
)
