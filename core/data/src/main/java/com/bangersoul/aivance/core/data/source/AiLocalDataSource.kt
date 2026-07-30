package com.bangersoul.aivance.core.data.source

import com.bangersoul.aivance.core.common.model.AIConversation
import com.bangersoul.aivance.core.common.model.AIMessage
import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.model.AnalyticsEvent
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.AiAnalyticsDao
import com.bangersoul.aivance.core.database.model.AIConversationEntity
import com.bangersoul.aivance.core.database.model.ProviderConfigurationEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

interface AiLocalDataSource {
    fun getConversations(): Flow<List<AIConversation>>
    suspend fun saveConversation(conversation: AIConversation)
    suspend fun saveMessage(message: AIMessage)
    
    fun getProviderConfigs(): Flow<List<AiProviderConfig>>
    suspend fun getProviderConfig(provider: String): AiProviderConfig?
    suspend fun saveProviderConfig(config: AiProviderConfig)
    
    fun getAnalyticsEvents(): Flow<List<AnalyticsEvent>>
    suspend fun saveAnalyticsEvent(event: AnalyticsEvent)
}

class AiLocalDataSourceImpl @Inject constructor(
    private val aiAnalyticsDao: AiAnalyticsDao
) : AiLocalDataSource {

    override fun getConversations(): Flow<List<AIConversation>> {
        return aiAnalyticsDao.getConversationsWithMessages().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveConversation(conversation: AIConversation) {
        val entity = AIConversationEntity(
            id = conversation.id,
            title = conversation.title,
            createdAt = Instant.ofEpochMilli(conversation.createdDate),
            updatedAt = Instant.ofEpochMilli(conversation.lastUpdated)
        )
        aiAnalyticsDao.insertConversation(entity)
    }

    override suspend fun saveMessage(message: AIMessage) {
        aiAnalyticsDao.insertMessage(message.toEntity())
    }

    override fun getProviderConfigs(): Flow<List<AiProviderConfig>> {
        return aiAnalyticsDao.getAllProviderConfigs().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getProviderConfig(provider: String): AiProviderConfig? {
        return aiAnalyticsDao.getProviderConfig(provider)?.toDomain()
    }

    override suspend fun saveProviderConfig(config: AiProviderConfig) {
        val entity = ProviderConfigurationEntity(
            provider = config.providerId,
            apiKey = config.apiKey,
            baseUrl = config.customBaseUrl,
            settings = mapOf(
                "selectedModel" to config.selectedModel,
                "temperature" to config.temperature.toString(),
                "maxTokens" to config.maxTokens.toString()
            )
        )
        aiAnalyticsDao.insertProviderConfig(entity)
    }

    override fun getAnalyticsEvents(): Flow<List<AnalyticsEvent>> {
        return aiAnalyticsDao.getAnalyticsEvents().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveAnalyticsEvent(event: AnalyticsEvent) {
        aiAnalyticsDao.insertAnalyticsEvent(event.toEntity())
    }
}
