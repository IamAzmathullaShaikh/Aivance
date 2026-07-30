package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.model.AIConversation
import com.bangersoul.aivance.core.common.model.AIMessage
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface AiRepository {
    suspend fun analyzeText(text: String, prompt: String): CoreResult<String>
    suspend fun startChatSession(providerId: String, modelName: String): CoreResult<AIConversation>
    suspend fun sendMessage(conversationId: String, message: String): CoreResult<AIMessage>
    fun getConversation(id: String): Flow<CoreResult<AIConversation>>
    fun getConversations(): Flow<CoreResult<List<AIConversation>>>
}
