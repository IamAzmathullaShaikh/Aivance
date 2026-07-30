package com.bangersoul.aivance.sdk.api

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.sdk.core.BaseProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.model.AiMessage
import kotlinx.coroutines.flow.Flow

/**
 * Interface for AI providers that support text generation and chat.
 * Inherits from [BaseProvider] to manage metadata and lifecycle.
 */
abstract class AIProvider(
    metadata: ProviderMetadata,
    capabilities: Set<ProviderCapability>
) : BaseProvider(metadata, capabilities) {
    
    /**
     * Generates text based on a single prompt.
     * @param prompt The input text to generate from.
     * @return Result containing the generated text or an error.
     */
    abstract suspend fun generateText(prompt: String): Result<String>

    /**
     * Executes a chat conversation with a list of messages.
     * @param messages The history of the conversation.
     * @return Result containing the next assistant message content or an error.
     */
    abstract suspend fun chat(messages: List<AiMessage>): Result<String>

    /**
     * Streams text generation based on a prompt.
     * @param prompt The input text to generate from.
     * @return A flow of text chunks as they are generated.
     */
    abstract fun streamText(prompt: String): Flow<String>

    /**
     * Lists the available models for this provider.
     * @return Result containing a list of model identifiers or an error.
     */
    abstract suspend fun listModels(): Result<List<String>>

    /**
     * Streams a chat conversation response.
     * @param messages The history of the conversation.
     * @return A flow of result chunks containing the generated text or an error.
     */
    abstract fun streamChat(messages: List<AiMessage>): Flow<Result<String>>
}
