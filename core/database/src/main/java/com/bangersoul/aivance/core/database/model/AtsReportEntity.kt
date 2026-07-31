package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ats_reports",
    foreignKeys = [
        ForeignKey(
            entity = ResumeVersionEntity::class,
            parentColumns = ["id"],
            childColumns = ["resumeVersionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = JobDescriptionEntity::class,
            parentColumns = ["id"],
            childColumns = ["jobDescriptionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["resumeVersionId"]),
        Index(value = ["jobDescriptionId"])
    ]
)
data class AtsReportEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val resumeVersionId: Long,
    val jobDescriptionId: Long,
    val overallScore: Int,
    val matchPercentage: Int,
    val matchedKeywords: String, // JSON or comma-separated
    val missingKeywords: String, // JSON or comma-separated
    val sectionScores: String, // JSON map
    val optimizationTips: String, // JSON list
    val dateGenerated: Long = System.currentTimeMillis()
)
