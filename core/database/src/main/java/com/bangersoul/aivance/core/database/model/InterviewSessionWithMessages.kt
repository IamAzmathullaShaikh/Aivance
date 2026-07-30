package com.bangersoul.aivance.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class InterviewSessionWithMessages(
    @Embedded val session: InterviewSessionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val messages: List<InterviewMessageEntity>
)
