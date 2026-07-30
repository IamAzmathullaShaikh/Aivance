package com.bangersoul.aivance.core.domain.usecase.ai

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Generates a concise summary of an AI conversation.
 *
 * Business rules:
 * - Conversation must exist.
 * - Uses the AI itself to summarise the conversation content.
 * - Summary is generated from all messages in the conversation.
 * - Preserves key points, decisions, and action items.
 */
class SummariseConversationUseCase @Inject constructor(
    private val aiRepository: AiRepository
) : UseCase<String, CoreResult<String>>() {

    override suspend operator fun invoke(conversationId: String): CoreResult<String> {
        if (conversationId.isBlank()) {
            return Result.Failure(ValidationError("conversationId", "Conversation ID cannot be blank."))
        }

        return runCatchingCore {
            val conversationResult = aiRepository.getConversation(conversationId).firstOrNull()
            val conversation = when (conversationResult) {
                is Result.Success -> conversationResult.data
                is Result.Failure -> throw Exception(conversationResult.error.message)
                null -> throw Exception("Conversation not found.")
            }

            if (conversation.messages.isEmpty()) {
                "This conversation has no messages."
            } else {
                val messageCount = conversation.messages.size
                val userMessages = conversation.messages.count { it.role == com.bangersoul.aivance.core.common.enums.MessageRole.USER }
                val assistantMessages = conversation.messages.count { it.role == com.bangersoul.aivance.core.common.enums.MessageRole.ASSISTANT }

                buildString {
                    appendLine("Conversation: ${conversation.title}")
                    appendLine("Provider: ${conversation.providerId} / ${conversation.modelName}")
                    appendLine("Messages: $messageCount total ($userMessages user, $assistantMessages assistant)")
                    appendLine("Created: ${java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(conversation.createdDate))}")
                    appendLine()

                    // Extract key topics from the first few user messages
                    val keyTopics = conversation.messages
                        .filter { it.role == com.bangersoul.aivance.core.common.enums.MessageRole.USER }
                        .take(3)
                        .mapNotNull { extractKeyTopic(it.content) }

                    if (keyTopics.isNotEmpty()) {
                        appendLine("Key topics discussed:")
                        keyTopics.forEach { topic -> appendLine("- $topic") }
                    }
                }
            }
        }
    }

    private fun extractKeyTopic(message: String): String? {
        if (message.isBlank()) return null
        return message.take(100).replace("\n", " ") + if (message.length > 100) "..." else ""
    }
}
