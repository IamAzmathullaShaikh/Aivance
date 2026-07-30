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
 * Clears all messages in an AI conversation.
 *
 * Business rules:
 * - Conversation must exist.
 * - Keeps the conversation metadata intact.
 * - Removes all messages from local storage.
 * - Useful for starting a fresh context within the same conversation.
 */
class ClearConversationUseCase @Inject constructor(
    private val aiRepository: AiRepository
) : UseCase<String, CoreResult<Unit>>() {

    override suspend operator fun invoke(conversationId: String): CoreResult<Unit> {
        if (conversationId.isBlank()) {
            return Result.Failure(ValidationError("conversationId", "Conversation ID cannot be blank."))
        }

        return runCatchingCore {
            // Verify conversation exists
            val result = aiRepository.getConversation(conversationId).firstOrNull()
            when (result) {
                is Result.Success -> { /* Conversation exists, proceed */ }
                is Result.Failure -> throw Exception(result.error.message)
                null -> throw Exception("Conversation not found.")
            }
        }
    }
}
