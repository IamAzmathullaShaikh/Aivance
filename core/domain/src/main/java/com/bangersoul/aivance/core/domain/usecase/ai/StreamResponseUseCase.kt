package com.bangersoul.aivance.core.domain.usecase.ai

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.enums.MessageRole
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.usecase.FlowUseCase
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import com.bangersoul.aivance.sdk.model.AiMessage as SdkAiMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class StreamResponseRequest(
    val conversationId: String,
    val message: String
)

/**
 * Streams an AI response for a given message.
 *
 * Business rules:
 * - Conversation must exist.
 * - Uses streaming endpoint for real-time token delivery.
 * - Each emitted string is a partial response chunk.
 * - The stream completes when the full response is generated.
 */
class StreamResponseUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val providerManager: ProviderManager
) : FlowUseCase<StreamResponseRequest, String>() {

    override fun invoke(input: StreamResponseRequest): Flow<String> {
        return flow {
            if (input.conversationId.isBlank()) {
                throw IllegalArgumentException("Conversation ID cannot be blank.")
            }
            if (input.message.isBlank()) {
                throw IllegalArgumentException("Message cannot be blank.")
            }

            // Persist the user message first
            aiRepository.sendMessage(input.conversationId, input.message)

            // Stream response from AI provider
            val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Streaming)
                as? com.bangersoul.aivance.sdk.api.AIProvider
                ?: throw Exception("No streaming AI provider available.")

            val sdkMessages = listOf(SdkAiMessage(MessageRole.USER, input.message))
            var fullResponse = ""

            provider.streamChat(sdkMessages).collect { chunkResult ->
                when (chunkResult) {
                    is Result.Success -> {
                        emit(chunkResult.data)
                        fullResponse += chunkResult.data
                    }
                    is Result.Failure -> throw Exception(chunkResult.error.message)
                }
            }

            // Persist the complete AI response
            if (fullResponse.isNotBlank()) {
                aiRepository.sendMessage(input.conversationId, fullResponse)
            }
        }
    }
}
