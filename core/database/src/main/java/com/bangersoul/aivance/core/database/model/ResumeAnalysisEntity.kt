package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "resume_analyses",
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
data class ResumeAnalysisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val resumeId: Long,
    val jobDescription: String,
    val score: Int,
    val matchedKeywords: String,
    val missingKeywords: String,
    val feedback: String,
    val date: Long
)
