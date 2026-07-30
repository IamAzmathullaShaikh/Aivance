package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "ai_conversations")
data class AIConversationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
