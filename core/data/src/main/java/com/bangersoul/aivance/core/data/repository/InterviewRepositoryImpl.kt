package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.enums.MessageSender
import com.bangersoul.aivance.core.common.model.InterviewFeedback
import com.bangersoul.aivance.core.common.model.InterviewMessage
import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.source.InterviewLocalDataSource
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InterviewRepositoryImpl @Inject constructor(
    private val localDataSource: InterviewLocalDataSource,
    private val providerManager: ProviderManager
) : InterviewRepository {

    override fun getSessions(): Flow<CoreResult<List<InterviewSession>>> {
        return localDataSource.getSessions().map { runCatchingCore { it } }
    }

    override fun getSessionById(id: String): Flow<CoreResult<InterviewSession>> {
        return localDataSource.getSessionById(id.toLongOrNull() ?: 0L).map {
            runCatchingCore { it ?: throw Exception("Session not found") }
        }
    }

    override suspend fun startSession(role: String, company: String, difficulty: InterviewDifficulty): CoreResult<InterviewSession> = runCatchingCore {
        val session = InterviewSession(
            id = "0",
            targetRole = role,
            companyName = company,
            difficulty = difficulty
        )
        val id = localDataSource.saveSession(session)
        session.copy(id = id.toString())
    }

    override suspend fun submitMessage(sessionId: String, text: String): CoreResult<InterviewMessage> = runCatchingCore {
        val message = InterviewMessage(
            id = "0",
            sender = MessageSender.USER,
            text = text
        )
        localDataSource.saveMessage(sessionId.toLongOrNull() ?: 0L, message)
        message
    }

    override suspend fun generateFeedback(sessionId: String): CoreResult<InterviewFeedback> = runCatchingCore {
        // Call AI to generate feedback
        InterviewFeedback(overallScore = 85, detailedSummary = "Good job!")
    }

    override suspend fun completeSession(sessionId: String): CoreResult<Unit> = runCatchingCore {
        // Mark session as completed
    }

    override suspend fun deleteSession(sessionId: String): CoreResult<Unit> = runCatchingCore {
        localDataSource.deleteSession(sessionId.toLongOrNull() ?: 0L)
    }
}
