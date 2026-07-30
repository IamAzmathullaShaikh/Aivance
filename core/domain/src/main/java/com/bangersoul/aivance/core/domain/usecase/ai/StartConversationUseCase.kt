package com.bangersoul.aivance.core.domain.usecase.ai

import com.bangersoul.aivance.core.common.model.AIConversation
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class StartConversationRequest(
    val title: String = "New Conversation",
    val providerId: String = "GEMINI",
    val modelName: String = "gemini-1.5-flash"
)

/**
 * Starts a new AI conversation/chat session.
 *
 * Business rules:
 * - Provider must be a supported provider ID.
 * - Creates a new conversation with the specified AI model.
 * - Saves the conversation to local storage for history.
 */
class StartConversationUseCase @Inject constructor(
    private val aiRepository: AiRepository
) : UseCase<StartConversationRequest, CoreResult<AIConversation>>() {

    override suspend operator fun invoke(input: StartConversationRequest): CoreResult<AIConversation> {
        if (input.providerId.isBlank()) {
            return Result.Failure(ValidationError("providerId", "Provider ID cannot be blank."))
        }
        if (input.modelName.isBlank()) {
            return Result.Failure(ValidationError("modelName", "Model name cannot be blank."))
        }

        return runCatchingCore {
            val result = aiRepository.startChatSession(input.providerId, input.modelName)
            when (result) {
                is Result.Success -> result.data.copy(title = input.title)
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }
}
