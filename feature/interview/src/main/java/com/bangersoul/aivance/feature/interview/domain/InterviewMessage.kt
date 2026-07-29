package com.bangersoul.aivance.feature.interview.domain

import java.util.UUID

enum class MessageRole {
    AI,
    User
}

data class InterviewMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
