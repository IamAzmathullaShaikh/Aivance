package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bangersoul.aivance.core.common.model.InterviewFeedback
import java.time.Instant

@Entity(
    tableName = "interview_messages",
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
data class InterviewMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val role: String,
    val text: String,
    val timestamp: Instant = Instant.now(),
    val feedback: InterviewFeedback? = null
)
