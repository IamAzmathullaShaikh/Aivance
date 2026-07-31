package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.bangersoul.aivance.core.common.model.InterviewFeedback
import java.time.Instant

@Entity(
    tableName = "interview_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ResumeVersionEntity::class,
            parentColumns = ["id"],
            childColumns = ["resumeVersionId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = JobEntity::class,
            parentColumns = ["id"],
            childColumns = ["jobId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["targetRole"]),
        Index(value = ["resumeVersionId"]),
        Index(value = ["jobId"])
    ]
)
data class InterviewSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val resumeVersionId: Long? = null,
    val jobId: Long? = null,
    val targetRole: String,
    val type: String = "BEHAVIORAL", // TECHNICAL, HR, BEHAVIORAL
    val difficulty: String,
    val dateStarted: Instant = Instant.now(),
    val dateEnded: Instant? = null,
    val isCompleted: Boolean = false,
    val overallFeedback: InterviewFeedback? = null
)
