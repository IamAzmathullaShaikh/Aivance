package com.bangersoul.aivance.core.domain.usecase.ai

import com.bangersoul.aivance.core.common.model.AIMessage
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class SendMessageRequest(
    val conversationId: String,
    val message: String
)

/**
 * Sends a message in an AI conversation and gets a response.
 *
 * Business rules:
 * - Conversation must exist.
 * - Message must not be blank.
 * - Returns both the user message and the AI response.
 * - Messages are persisted to local storage.
 */
class SendMessageUseCase @Inject constructor(
    private val aiRepository: AiRepository
) : UseCase<SendMessageRequest, CoreResult<AIMessage>>() {

    override suspend operator fun invoke(input: SendMessageRequest): CoreResult<AIMessage> {
        if (input.conversationId.isBlank()) {
            return Result.Failure(ValidationError("conversationId", "Conversation ID cannot be blank."))
        }
        if (input.message.isBlank()) {
            return Result.Failure(ValidationError("message", "Message cannot be blank."))
        }
        if (input.message.length > 50000) {
            return Result.Failure(ValidationError("message", "Message must not exceed 50,000 characters."))
        }

        return runCatchingCore {
            val result = aiRepository.sendMessage(input.conversationId, input.message)
            when (result) {
                is Result.Success -> result.data
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }
}
