package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cover_letters")
data class CoverLetterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val resumeVersionId: Long?,
    val jobId: Long?,
    val recruiterId: String?,
    val primaryVersionId: Long? = null,
    val company: String,
    val role: String,
    val dateCreated: Long = System.currentTimeMillis()
)
