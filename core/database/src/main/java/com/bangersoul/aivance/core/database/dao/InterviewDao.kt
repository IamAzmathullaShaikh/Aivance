package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bangersoul.aivance.core.database.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InterviewDao {
    @Transaction
    @Query("SELECT * FROM interview_sessions ORDER BY dateStarted DESC")
    fun getInterviewSessions(): Flow<List<InterviewSessionWithMessages>>

    @Transaction
    @Query("SELECT * FROM interview_sessions WHERE id = :sessionId")
    suspend fun getInterviewSessionWithMessagesById(sessionId: Long): InterviewSessionWithMessages?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: InterviewSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: InterviewMessageEntity): Long

    @Delete
    suspend fun deleteSession(session: InterviewSessionEntity)

    // Questions
    @Query("SELECT * FROM interview_questions WHERE sessionId = :sessionId")
    fun getQuestionsForSession(sessionId: Long): Flow<List<InterviewQuestionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: InterviewQuestionEntity): Long

    // Evaluations
    @Query("SELECT * FROM interview_evaluations WHERE messageId = :messageId")
    suspend fun getEvaluationForMessage(messageId: Long): InterviewEvaluationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvaluation(evaluation: InterviewEvaluationEntity): Long
}
