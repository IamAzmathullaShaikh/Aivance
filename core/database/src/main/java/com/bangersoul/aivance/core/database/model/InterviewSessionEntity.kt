package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bangersoul.aivance.core.common.model.InterviewFeedback
import java.time.Instant

@Entity(
    tableName = "interview_sessions",
    indices = [Index(value = ["targetRole"])]
)
data class InterviewSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetRole: String,
    val difficulty: String,
    val dateStarted: Instant = Instant.now(),
    val isCompleted: Boolean = false,
    val overallFeedback: InterviewFeedback? = null
)
