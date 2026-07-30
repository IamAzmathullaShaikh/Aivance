package com.bangersoul.aivance.sdk.model

import com.bangersoul.aivance.core.common.enums.MessageRole

/**
 * Simplified message model for AI SDK operations.
 */
data class AiMessage(
    val role: MessageRole,
    val content: String,
    val images: List<String> = emptyList()
)
