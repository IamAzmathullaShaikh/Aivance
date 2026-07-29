package com.bangersoul.aivance.core.network.ai

enum class AiRole {
    User,
    Assistant,
    System
}

data class AiMessage(
    val role: AiRole,
    val content: String
)
