package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bangersoul.aivance.core.database.model.AIConversationEntity
import com.bangersoul.aivance.core.database.model.AIConversationWithMessages
import com.bangersoul.aivance.core.database.model.AIMessageEntity
import com.bangersoul.aivance.core.database.model.AnalyticsEventEntity
import com.bangersoul.aivance.core.database.model.ProviderConfigurationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiAnalyticsDao {
    // AI Conversations
    @Transaction
    @Query("SELECT * FROM ai_conversations")
    fun getConversationsWithMessages(): Flow<List<AIConversationWithMessages>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: AIConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AIMessageEntity)

    @Query("DELETE FROM ai_conversations WHERE updatedAt < :beforeTimestamp")
    suspend fun deleteOldConversations(beforeTimestamp: Long): Int

    @Query("SELECT COUNT(*) FROM ai_conversations")
    suspend fun getConversationCount(): Int

    // Provider Configurations
    @Query("SELECT * FROM provider_configurations")
    fun getAllProviderConfigs(): Flow<List<ProviderConfigurationEntity>>

    @Query("SELECT * FROM provider_configurations WHERE provider = :provider")
    suspend fun getProviderConfig(provider: String): ProviderConfigurationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviderConfig(config: ProviderConfigurationEntity)

    // Analytics
    @Query("SELECT * FROM analytics_events ORDER BY timestamp DESC")
    fun getAnalyticsEvents(): Flow<List<AnalyticsEventEntity>>

    @Query("SELECT * FROM analytics_events ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    fun getAnalyticsEventsPaginated(limit: Int, offset: Int): Flow<List<AnalyticsEventEntity>>

    @Query("SELECT COUNT(*) FROM analytics_events")
    suspend fun getEventCount(): Int

    @Query("DELETE FROM analytics_events")
    suspend fun deleteAllEvents(): Int

    @Query("DELETE FROM analytics_events WHERE timestamp < :beforeTimestamp")
    suspend fun deleteEventsBefore(beforeTimestamp: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyticsEvent(event: AnalyticsEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalyticsEvents(events: List<AnalyticsEventEntity>)
}
