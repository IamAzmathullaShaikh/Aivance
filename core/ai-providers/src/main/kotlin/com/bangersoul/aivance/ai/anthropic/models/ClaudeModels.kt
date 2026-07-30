package com.bangersoul.aivance.ai.anthropic.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaudeMessageRequest(
    val model: String,
    val messages: List<ClaudeMessage>,
    @SerialName("max_tokens") val maxTokens: Int = 4096,
    val system: String? = null,
    val temperature: Float? = null,
    val stream: Boolean = false
)

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String
)

@Serializable
data class ClaudeMessageResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<ClaudeContent>,
    val model: String,
    @SerialName("stop_reason") val stopReason: String?,
    @SerialName("stop_sequence") val stopSequence: String?,
    val usage: ClaudeUsage
)

@Serializable
data class ClaudeContent(
    val type: String,
    val text: String? = null
)

@Serializable
data class ClaudeUsage(
    @SerialName("input_tokens") val inputTokens: Int,
    @SerialName("output_tokens") val outputTokens: Int
)

@Serializable
data class ClaudeStreamEvent(
    val type: String,
    val index: Int? = null,
    val delta: ClaudeDelta? = null,
    val message: ClaudeMessageResponse? = null
)

@Serializable
data class ClaudeDelta(
    val type: String,
    val text: String? = null
)
