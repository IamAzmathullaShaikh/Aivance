package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "assistant_conversations")
data class AssistantConversationEntity(
    @PrimaryKey
    val id: String, // UUID
    val title: String,
    val activeJobId: Long? = null,
    val activeResumeVersionId: Long? = null,
    val lastIntent: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "assistant_messages",
    foreignKeys = [
        ForeignKey(
            entity = AssistantConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class AssistantMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: String,
    val role: String, // USER, ASSISTANT, SYSTEM
    val content: String,
    val actionButtonsJson: String? = null, // List of buttons with deep links
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "workflow_executions",
    foreignKeys = [
        ForeignKey(
            entity = AssistantConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class WorkflowExecutionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: String,
    val workflowType: String, // e.g. FULL_APPLICATION
    val currentStep: Int,
    val totalSteps: Int,
    val status: String, // IN_PROGRESS, COMPLETED, FAILED
    val stateJson: String? = null // Context data for the workflow
)
