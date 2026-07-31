package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "job_descriptions")
data class JobDescriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyName: String?,
    val jobTitle: String?,
    val rawText: String,
    val sourceUrl: String?,
    val extractedSkills: String?, // Comma-separated
    val dateCreated: Long = System.currentTimeMillis()
)
