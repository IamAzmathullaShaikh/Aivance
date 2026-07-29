package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val company: String,
    val role: String,
    val status: String,
    val dateApplied: Long,
    val salaryRange: String?,
    val notes: String?,
    val lastModified: Long
)
