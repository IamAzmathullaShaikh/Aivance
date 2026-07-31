package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "resume_versions",
    foreignKeys = [
        ForeignKey(
            entity = ResumeEntity::class,
            parentColumns = ["id"],
            childColumns = ["resumeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["resumeId"])]
)
data class ResumeVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val resumeId: Long,
    val versionName: String,
    val templateId: String = "modern",
    val lastModified: Long = System.currentTimeMillis()
)
