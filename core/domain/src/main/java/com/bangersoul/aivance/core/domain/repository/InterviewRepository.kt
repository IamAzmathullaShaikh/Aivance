package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.model.InterviewFeedback
import com.bangersoul.aivance.core.common.model.InterviewMessage
import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface InterviewRepository {
    fun getSessions(): Flow<CoreResult<List<InterviewSession>>>
    fun getSessionById(id: String): Flow<CoreResult<InterviewSession>>
    suspend fun startSession(role: String, company: String, difficulty: InterviewDifficulty): CoreResult<InterviewSession>
    suspend fun submitMessage(sessionId: String, text: String): CoreResult<InterviewMessage>
    suspend fun generateFeedback(sessionId: String): CoreResult<InterviewFeedback>
    suspend fun completeSession(sessionId: String): CoreResult<Unit>
    suspend fun deleteSession(sessionId: String): CoreResult<Unit>
}
