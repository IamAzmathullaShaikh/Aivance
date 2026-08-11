package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.model.InterviewFeedback
import com.bangersoul.aivance.core.common.model.InterviewMessage
import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.common.model.InterviewQuestion
import com.bangersoul.aivance.core.common.model.InterviewEvaluation
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.InterviewDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.dao.ResumeDao
import com.bangersoul.aivance.core.database.model.InterviewSessionWithMessages
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterviewRepositoryImpl @Inject constructor(
    private val interviewDao: InterviewDao,
    private val resumeDao: ResumeDao,
    private val jobDao: JobDao,
    private val providerManager: ProviderManager
) : InterviewRepository {

    private val json = Json { ignoreUnknownKeys = true }

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

    override fun getQuestions(sessionId: String): Flow<CoreResult<List<InterviewQuestion>>> {
        val sessionLongId = sessionId.toLongOrNull()
        return if (sessionLongId == null) {
            flow { emit(Result.Failure(DomainError("Invalid session id"))) }
        } else {
            interviewDao.getQuestionsForSession(sessionLongId).map { entities ->
                runCatchingCore { entities.map { it.toDomain() } }
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
        val id = sessionId.toLongOrNull() ?: throw Exception("Invalid ID")
        val sessionWithData = interviewDao.getInterviewSessionWithMessagesById(id) ?: throw Exception("Session not found")
        val session = sessionWithData.toDomain()

        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider available")

        val prompt = """
            Generate $count interview questions for a ${session.difficulty} level interview for the role of ${session.targetRole} at ${session.companyName}.
            Type of interview: ${session.type}.

            Return ONLY a JSON array of objects with:
            "text": String,
            "category": String (e.g. Technical, Behavioral),
            "difficulty": String (Easy, Medium, Hard),
            "expectedKeyPoints": [String],
            "idealAnswer": String
        """.trimIndent()

        val response = provider.generateText(prompt).getOrNull() ?: throw Exception("AI failed to generate questions")

        val jsonText = if (response.contains("```json")) {
            response.substringAfter("```json").substringBefore("```").trim()
        } else if (response.contains("[")) {
            response.substring(response.indexOf("["), response.lastIndexOf("]") + 1)
        } else response

        val questions = json.decodeFromString<List<InterviewQuestion>>(jsonText)
        questions.forEach { q ->
            interviewDao.insertQuestion(q.toEntity(id))
        }
    }

    override suspend fun persistPackQuestions(
        sessionId: String,
        questions: List<InterviewQuestion>
    ): CoreResult<Unit> = runCatchingCore {
        val id = sessionId.toLongOrNull() ?: throw Exception("Invalid ID")
        questions.forEach { question ->
            interviewDao.insertQuestion(question.toEntity(id))
        }
    }

    override suspend fun submitAnswer(sessionId: String, message: InterviewMessage): CoreResult<Unit> = runCatchingCore {
        val msgId = interviewDao.insertMessage(message.toEntity())
        evaluateAnswer(msgId.toString())
    }

    override suspend fun evaluateAnswer(messageId: String): CoreResult<Unit> = runCatchingCore {
        val msgId = messageId.toLongOrNull() ?: throw Exception("Invalid ID")
        val messageEntity = interviewDao.getMessageById(msgId) ?: throw Exception("Message not found")
        val sessionWithData = interviewDao.getInterviewSessionWithMessagesById(messageEntity.sessionId) ?: throw Exception("Session not found")

        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider available")

        val prompt = """
            Evaluate the following candidate interview answer for the role ${sessionWithData.session.targetRole}.
            Answer: "${messageEntity.text}"

            Return ONLY a JSON object with:
            "scoreClarity": Int (0-100),
            "scoreAccuracy": Int (0-100),
            "scoreTone": Int (0-100),
            "starMethodScore": Int (0-100, optional),
            "feedback": String,
            "improvementTips": [String]
        """.trimIndent()

        val response = provider.generateText(prompt).getOrNull() ?: throw Exception("AI evaluation failed")

        val jsonText = if (response.contains("```json")) {
            response.substringAfter("```json").substringBefore("```").trim()
        } else if (response.contains("{")) {
            response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1)
        } else response

        val evaluation = json.decodeFromString<InterviewEvaluation>(jsonText).copy(
            messageId = messageId
        )

        interviewDao.insertEvaluation(evaluation.toEntity())
    }

    override suspend fun completeSession(sessionId: String): CoreResult<Unit> = runCatchingCore {
        val id = sessionId.toLongOrNull() ?: throw Exception("Invalid ID")
        val entity = interviewDao.getInterviewSessionWithMessagesById(id) ?: throw Exception("Not found")

        // Generate AI feedback from the transcript before marking the session complete,
        // so the review screen shows a real evaluation instead of a placeholder.
        val feedback = generateSessionFeedback(entity)
        val updated = entity.session.copy(isCompleted = true, overallFeedback = feedback)
        interviewDao.insertSession(updated)
    }

    private suspend fun generateSessionFeedback(
        entity: InterviewSessionWithMessages
    ): InterviewFeedback? {
        val transcript = entity.messages
            .filter { it.role == "USER" || it.role == "ASSISTANT" }
            .joinToString("\n") { "${it.role}: ${it.text}" }
        if (transcript.isBlank()) return null

        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: return null

        val prompt = """
            You are an expert interview coach. Evaluate the candidate's performance for the role of ${entity.session.targetRole}.
            Interview transcript:
            $transcript

            Return ONLY a JSON object with:
            "overallScore": Int (0-100),
            "strengths": [String],
            "improvements": [String],
            "detailedSummary": String
        """.trimIndent()

        val response = provider.generateText(prompt).getOrNull() ?: return null
        val jsonText = if (response.contains("```json")) {
            response.substringAfter("```json").substringBefore("```").trim()
        } else if (response.contains("{")) {
            response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1)
        } else response
        return try {
            json.decodeFromString<InterviewFeedback>(jsonText)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteSession(sessionId: String): CoreResult<Unit> = runCatchingCore {
        val id = sessionId.toLongOrNull() ?: throw Exception("Invalid ID")
        val entity = interviewDao.getInterviewSessionWithMessagesById(id) ?: throw Exception("Not found")
        interviewDao.deleteSession(entity.session)
    }
}
