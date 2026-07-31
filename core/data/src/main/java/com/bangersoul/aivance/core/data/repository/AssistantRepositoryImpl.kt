package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.database.dao.AssistantDao
import com.bangersoul.aivance.core.database.model.AssistantMessageEntity
import com.bangersoul.aivance.core.domain.repository.AssistantConversation
import com.bangersoul.aivance.core.domain.repository.AssistantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantRepositoryImpl @Inject constructor(
    private val assistantDao: AssistantDao
) : AssistantRepository {

    override fun getConversations(): Flow<CoreResult<List<AssistantConversation>>> {
        return assistantDao.getConversations().map { entities ->
            runCatchingCore {
                entities.map { AssistantConversation(it.id, it.title, it.lastUpdatedAt) }
            }
        }
    }

    override suspend fun saveMessage(conversationId: String, role: String, content: String): CoreResult<Long> = runCatchingCore {
        assistantDao.insertMessage(
            AssistantMessageEntity(
                conversationId = conversationId,
                role = role,
                content = content
            )
        )
    }
}
