package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.model.InterviewMessage
import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.InterviewDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.dao.ResumeDao
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterviewRepositoryImpl @Inject constructor(
    private val interviewDao: InterviewDao,
    private val resumeDao: ResumeDao,
    private val jobDao: JobDao,
    private val providerManager: ProviderManager
) : InterviewRepository {

    override fun getSessions(): Flow<CoreResult<List<InterviewSession>>> {
        return interviewDao.getInterviewSessions().map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
    }

    override fun getSessionById(id: String): Flow<CoreResult<InterviewSession>> {
        return interviewDao.getInterviewSessions().map { list ->
            runCatchingCore {
                val entity = list.find { it.session.id.toString() == id } ?: throw Exception("Session not found")
                entity.toDomain()
            }
        }
    }

    override suspend fun startSession(
        role: String,
        company: String,
        difficulty: InterviewDifficulty,
        jobId: Long?,
        resumeVersionId: Long?,
        type: String
    ): CoreResult<InterviewSession> = runCatchingCore {
        val session = InterviewSession(
            id = "0",
            targetRole = role,
            companyName = company,
            difficulty = difficulty,
            jobId = jobId,
            resumeVersionId = resumeVersionId,
            type = type
        )
        val id = interviewDao.insertSession(session.toEntity())
        session.copy(id = id.toString())
    }

    override suspend fun generateQuestions(sessionId: String, count: Int): CoreResult<Unit> = runCatchingCore {
        // Logic to generate questions via AI
    }

    override suspend fun submitAnswer(sessionId: String, message: InterviewMessage): CoreResult<Unit> = runCatchingCore {
        interviewDao.insertMessage(message.toEntity())
        evaluateAnswer(message.id)
    }

    override suspend fun evaluateAnswer(messageId: String): CoreResult<Unit> = runCatchingCore {
        // Logic for AI evaluation
    }

    override suspend fun completeSession(sessionId: String): CoreResult<Unit> = runCatchingCore {
        val id = sessionId.toLongOrNull() ?: throw Exception("Invalid ID")
        val entity = interviewDao.getInterviewSessionWithMessagesById(id) ?: throw Exception("Not found")
        val updated = entity.session.copy(isCompleted = true)
        interviewDao.insertSession(updated)
    }

    override suspend fun deleteSession(sessionId: String): CoreResult<Unit> = runCatchingCore {
        val id = sessionId.toLongOrNull() ?: throw Exception("Invalid ID")
        val entity = interviewDao.getInterviewSessionWithMessagesById(id) ?: throw Exception("Not found")
        interviewDao.deleteSession(entity.session)
    }
}
