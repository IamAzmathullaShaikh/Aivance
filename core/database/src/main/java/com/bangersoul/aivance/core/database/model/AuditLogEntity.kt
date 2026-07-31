package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val action: String, // e.g., "RESUME_DELETED", "PROVIDER_ADDED"
    val module: String, // e.g., "RESUME", "CRM"
    val timestamp: Long = System.currentTimeMillis(),
    val metadataJson: String? = null,
    val severity: String = "INFO" // INFO, WARNING, SECURITY
)
