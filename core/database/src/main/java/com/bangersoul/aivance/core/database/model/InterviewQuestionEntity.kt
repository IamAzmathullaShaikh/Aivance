package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "interview_questions",
    foreignKeys = [
        ForeignKey(
            entity = InterviewSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class InterviewQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long?, // Nullable if it's a generic knowledge base question
    val text: String,
    val category: String, // BEHAVIORAL, TECHNICAL, etc.
    val difficulty: String,
    val expectedKeyPoints: String?, // Comma separated or JSON
    val idealAnswer: String? = null,
    val isFavorite: Boolean = false
)
