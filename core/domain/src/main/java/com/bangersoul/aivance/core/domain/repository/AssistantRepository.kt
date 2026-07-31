package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface AssistantRepository {
    fun getConversations(): Flow<CoreResult<List<AssistantConversation>>>
    suspend fun saveMessage(conversationId: String, role: String, content: String): CoreResult<Long>
}

data class AssistantConversation(
    val id: String,
    val title: String,
    val lastUpdatedAt: Long
)
