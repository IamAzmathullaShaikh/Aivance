package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cover_letter_versions",
    foreignKeys = [
        ForeignKey(
            entity = CoverLetterEntity::class,
            parentColumns = ["id"],
            childColumns = ["coverLetterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["coverLetterId"])]
)
data class CoverLetterVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val coverLetterId: Long,
    val versionName: String,
    val templateId: String = "modern",
    val writingStyle: String = "PROFESSIONAL",
    val state: String = "DRAFT", // DRAFT, PUBLISHED, ARCHIVED
    val lastModified: Long = System.currentTimeMillis()
)
