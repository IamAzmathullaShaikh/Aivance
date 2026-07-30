package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "resume_sections",
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
data class ResumeSectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val resumeId: Long,
    val title: String,
    val content: String,
    val sectionOrder: Int
)
