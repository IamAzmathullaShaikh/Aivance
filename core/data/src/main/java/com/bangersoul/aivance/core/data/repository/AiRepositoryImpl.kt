package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.AIConversation
import com.bangersoul.aivance.core.common.model.AIMessage
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.onSuccess
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.source.AiLocalDataSource
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import com.bangersoul.aivance.sdk.model.AiMessage as SdkAiMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val localDataSource: AiLocalDataSource,
    private val providerManager: ProviderManager
) : AiRepository {

    private fun getProvider(): AIProvider {
        return providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider available")
    }

    override suspend fun analyzeText(text: String, prompt: String): Result<String> = runCatchingCore {
        getProvider().generateText("$prompt\n\n$text").getOrNull() ?: throw Exception("AI analysis failed")
    }

    override fun streamAnalyzeText(text: String, prompt: String): Flow<String> = flow {
        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Streaming) as? AIProvider
        if (provider != null) {
            provider.streamText("$prompt\n\n$text").collect { chunk -> emit(chunk) }
        } else {
            // Graceful fallback: non-streaming provider emits the full answer once.
            val full = getProvider().generateText("$prompt\n\n$text").getOrNull()
                ?: throw Exception("AI analysis failed")
            emit(full)
        }
    }

    override suspend fun startChatSession(providerId: String, modelName: String): Result<AIConversation> = runCatchingCore {
        val conversation = AIConversation(
            id = System.currentTimeMillis().toString(),
            title = "New Conversation",
            providerId = providerId,
            modelName = modelName
        )
        localDataSource.saveConversation(conversation)
        conversation
    }

    override suspend fun sendMessage(conversationId: String, message: String): Result<AIMessage> = runCatchingCore {
        val aiMessage = AIMessage(
            id = System.currentTimeMillis().toString(),
            conversationId = conversationId,
            role = com.bangersoul.aivance.core.common.enums.MessageRole.USER,
            content = message
        )
        localDataSource.saveMessage(aiMessage)
        
        // Use provider to get response
        val provider = getProvider()
        val sdkMessage = SdkAiMessage(aiMessage.role, aiMessage.content)
        val response = provider.chat(listOf(sdkMessage))
        
        response.onSuccess { content ->
            val responseMessage = AIMessage(
                id = (System.currentTimeMillis() + 1).toString(),
                conversationId = conversationId,
                role = com.bangersoul.aivance.core.common.enums.MessageRole.ASSISTANT,
                content = content
            )
            localDataSource.saveMessage(responseMessage)
        }
        
        aiMessage
    }

    override fun getConversation(id: String): Flow<Result<AIConversation>> {
        return localDataSource.getConversations().map { convs ->
            runCatchingCore { convs.find { it.id == id } ?: throw Exception("Conversation not found") }
        }
    }

    override fun getConversations(): Flow<Result<List<AIConversation>>> {
        return localDataSource.getConversations().map { runCatchingCore { it } }
    }
}
