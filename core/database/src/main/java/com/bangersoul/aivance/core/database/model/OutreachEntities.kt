package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outreach_drafts",
    foreignKeys = [
        ForeignKey(
            entity = RecruiterEntity::class,
            parentColumns = ["id"],
            childColumns = ["recruiterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = JobEntity::class,
            parentColumns = ["id"],
            childColumns = ["jobId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["recruiterId"]), Index(value = ["jobId"])]
)
data class OutreachDraftEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recruiterId: String,
    val jobId: Long?,
    val type: String,
    val content: String,
    val dateCreated: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "communication_history",
    foreignKeys = [
        ForeignKey(
            entity = RecruiterEntity::class,
            parentColumns = ["id"],
            childColumns = ["recruiterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recruiterId"])]
)
data class CommunicationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recruiterId: String,
    val messageType: String,
    val content: String,
    val sentDate: Long,
    val status: String,
    val notes: String?,
    val nextFollowUpDate: Long? = null
)
