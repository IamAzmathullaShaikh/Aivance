package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bangersoul.aivance.core.database.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantDao {

    @Query("SELECT * FROM assistant_conversations ORDER BY lastUpdatedAt DESC")
    fun getConversations(): Flow<List<AssistantConversationEntity>>

    @Query("SELECT * FROM assistant_conversations WHERE id = :id")
    suspend fun getConversationById(id: String): AssistantConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: AssistantConversationEntity)

    @Update
    suspend fun updateConversation(conversation: AssistantConversationEntity)

    @Query("SELECT * FROM assistant_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<AssistantMessageEntity>>

    @Insert
    suspend fun insertMessage(message: AssistantMessageEntity): Long

    @Query("SELECT * FROM workflow_executions WHERE conversationId = :conversationId")
    fun getWorkflowsForConversation(conversationId: String): Flow<List<WorkflowExecutionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkflowExecution(execution: WorkflowExecutionEntity): Long
}
