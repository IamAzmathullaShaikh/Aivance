package com.bangersoul.aivance.core.domain.usecase.ai

import com.bangersoul.aivance.core.common.model.AIConversation
import com.bangersoul.aivance.core.common.model.AIMessage
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class RegenerateResponseRequest(
    val conversationId: String
)

/**
 * Regenerates the last AI response in a conversation.
 *
 * Business rules:
 * - Conversation must exist and have messages.
 * - Re-sends the last user message to the AI.
 * - Removes the previous AI response before regenerating.
 * - Useful when the user is not satisfied with the response.
 */
class RegenerateResponseUseCase @Inject constructor(
    private val aiRepository: AiRepository
) : UseCase<RegenerateResponseRequest, CoreResult<AIMessage>>() {

    override suspend operator fun invoke(input: RegenerateResponseRequest): CoreResult<AIMessage> {
        if (input.conversationId.isBlank()) {
            return Result.Failure(ValidationError("conversationId", "Conversation ID cannot be blank."))
        }

        return runCatchingCore {
            val conversationResult = aiRepository.getConversation(input.conversationId).firstOrNull()
            val conversation = when (conversationResult) {
                is Result.Success -> conversationResult.data
                is Result.Failure -> throw Exception(conversationResult.error.message)
                null -> throw Exception("Conversation not found.")
            }

            if (conversation.messages.isEmpty()) {
                throw Exception("No messages to regenerate from.")
            }

            val lastUserMessage = conversation.messages.lastOrNull { message ->
                message.role == com.bangersoul.aivance.core.common.enums.MessageRole.USER
            }

            if (lastUserMessage == null) {
                throw Exception("No user message found to regenerate from.")
            }

            val result = aiRepository.sendMessage(input.conversationId, lastUserMessage.content)
            when (result) {
                is Result.Success -> result.data
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }
}
