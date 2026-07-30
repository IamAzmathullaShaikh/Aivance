package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bangersoul.aivance.core.database.model.InterviewMessageEntity
import com.bangersoul.aivance.core.database.model.InterviewSessionEntity
import com.bangersoul.aivance.core.database.model.InterviewSessionWithMessages
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewDao {
    @Transaction
    @Query("SELECT * FROM interview_sessions ORDER BY dateStarted DESC")
    fun getInterviewSessions(): Flow<List<InterviewSessionWithMessages>>

    @Transaction
    @Query("SELECT * FROM interview_sessions WHERE id = :sessionId")
    fun getInterviewSessionWithMessages(sessionId: Long): Flow<InterviewSessionWithMessages?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: InterviewSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: InterviewMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<InterviewMessageEntity>)

    @Delete
    suspend fun deleteSession(session: InterviewSessionEntity)

    @Transaction
    suspend fun deleteSessionAndMessages(sessionId: Long) {
        deleteMessagesForSession(sessionId)
        deleteSessionById(sessionId)
    }

    @Query("DELETE FROM interview_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Long)

    @Query("DELETE FROM interview_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)
}
