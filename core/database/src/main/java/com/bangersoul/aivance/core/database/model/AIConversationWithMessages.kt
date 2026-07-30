package com.bangersoul.aivance.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class AIConversationWithMessages(
    @Embedded val conversation: AIConversationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "conversationId"
    )
    val messages: List<AIMessageEntity>
)
