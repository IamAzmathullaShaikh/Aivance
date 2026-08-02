package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.model.AIConversation
import com.bangersoul.aivance.core.common.model.AIMessage
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun analyzeText(text: String, prompt: String): CoreResult<String>

    /**
     * Streaming variant of [analyzeText] — emits token chunks in real time from
     * a streaming-capable provider, falling back to a single emission for
     * providers that only support one-shot generation.
     */
    fun streamAnalyzeText(text: String, prompt: String): Flow<String>
    suspend fun startChatSession(providerId: String, modelName: String): CoreResult<AIConversation>
    suspend fun sendMessage(conversationId: String, message: String): CoreResult<AIMessage>
    fun getConversation(id: String): Flow<CoreResult<AIConversation>>
    fun getConversations(): Flow<CoreResult<List<AIConversation>>>
}
