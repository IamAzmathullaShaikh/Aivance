package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "interview_evaluations",
    foreignKeys = [
        ForeignKey(
            entity = InterviewMessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["messageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["messageId"])]
)
data class InterviewEvaluationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val messageId: Long,
    val scoreClarity: Int,
    val scoreAccuracy: Int,
    val scoreTone: Int,
    val starMethodScore: Int?,
    val feedback: String,
    val improvementTips: String?
)
