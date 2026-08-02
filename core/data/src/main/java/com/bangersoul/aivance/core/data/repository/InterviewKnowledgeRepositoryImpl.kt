package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.InterviewQuestion
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.InterviewDao
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.repository.InterviewKnowledgeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterviewKnowledgeRepositoryImpl @Inject constructor(
    private val interviewDao: InterviewDao,
    private val aiRepository: AiRepository
) : InterviewKnowledgeRepository {

    override fun getCommonQuestions(category: String): Flow<CoreResult<List<InterviewQuestion>>> = flow {
        seedLibraryIfEmpty()
        // Library questions are stored with sessionId = NULL; the old
        // getQuestionsForSession(0L) query never matched them. Query the library directly.
        emitAll(
            interviewDao.getLibraryQuestions().map { entities ->
                runCatchingCore {
                    entities
                        .filter { category.isBlank() || category == "ALL" || it.category == category }
                        .map { it.toDomain() }
                }
            }
        )
    }

    override fun getFavoriteQuestions(): Flow<CoreResult<List<InterviewQuestion>>> = flow {
        seedLibraryIfEmpty()
        emitAll(
            interviewDao.getFavoriteQuestions().map { entities ->
                runCatchingCore { entities.map { it.toDomain() } }
            }
        )
    }

    override suspend fun saveQuestionToLibrary(question: InterviewQuestion): CoreResult<Long> = runCatchingCore {
        interviewDao.insertQuestion(question.toEntity(null))
    }

    override suspend fun getIdealAnswer(questionId: Long): CoreResult<String> = runCatchingCore {
        val questionEntity = interviewDao.getQuestionById(questionId) ?: throw Exception("Question not found")
        val prompt = "Please generate a clear, professional, and comprehensive ideal answer for the following interview question. Use the STAR method if applicable."
        val result = aiRepository.analyzeText(questionEntity.text, prompt)
        when (result) {
            is Result.Success -> result.data
            is Result.Failure -> throw Exception(result.error.message)
        }
    }

    override suspend fun toggleFavorite(questionId: Long): CoreResult<Unit> = runCatchingCore {
        val current = interviewDao.getQuestionById(questionId) ?: throw Exception("Question not found")
        interviewDao.setFavorite(questionId, !current.isFavorite)
    }

    /**
     * Seeds a small starter library of common interview questions on first access
     * so the Question Bank tab is usable before any sessions are created.
     */
    private suspend fun seedLibraryIfEmpty() {
        val library = interviewDao.getLibraryQuestions().firstOrNull().orEmpty()
        if (library.isEmpty()) {
            STARTER_QUESTIONS.forEach { question ->
                interviewDao.insertQuestion(question.toEntity(null))
            }
        }
    }

    private companion object {
        val STARTER_QUESTIONS = listOf(
            InterviewQuestion(text = "Tell me about yourself and your background.", category = "BEHAVIORAL", difficulty = "EASY", expectedKeyPoints = listOf("Concise summary", "Relevant experience", "Career goals")),
            InterviewQuestion(text = "Why do you want to work here?", category = "BEHAVIORAL", difficulty = "MEDIUM", expectedKeyPoints = listOf("Company research", "Role alignment", "Values")),
            InterviewQuestion(text = "Describe a time you overcame a major challenge at work.", category = "BEHAVIORAL", difficulty = "MEDIUM", expectedKeyPoints = listOf("Situation", "Task", "Action", "Result")),
            InterviewQuestion(text = "Tell me about a conflict you had with a colleague and how you resolved it.", category = "BEHAVIORAL", difficulty = "HARD", expectedKeyPoints = listOf("Empathy", "Communication", "Outcome")),
            InterviewQuestion(text = "How do you stay current with new technologies in your field?", category = "TECHNICAL", difficulty = "EASY", expectedKeyPoints = listOf("Learning sources", "Hands-on practice", "Community")),
            InterviewQuestion(text = "Explain a complex technical concept to a non-technical audience.", category = "TECHNICAL", difficulty = "MEDIUM", expectedKeyPoints = listOf("Simplification", "Analogy", "Clarity")),
            InterviewQuestion(text = "Walk me through your approach to debugging a production issue.", category = "TECHNICAL", difficulty = "MEDIUM", expectedKeyPoints = listOf("Reproduce", "Isolate", "Fix", "Verify")),
            InterviewQuestion(text = "Describe the architecture of a project you're proud of.", category = "TECHNICAL", difficulty = "HARD", expectedKeyPoints = listOf("Trade-offs", "Scalability", "Decisions")),
            InterviewQuestion(text = "How do you motivate a team through a difficult period?", category = "LEADERSHIP", difficulty = "MEDIUM", expectedKeyPoints = listOf("Transparency", "Support", "Vision")),
            InterviewQuestion(text = "Tell me about a time you gave difficult feedback to a teammate.", category = "LEADERSHIP", difficulty = "HARD", expectedKeyPoints = listOf("Specificity", "Respect", "Follow-up")),
            InterviewQuestion(text = "What are your greatest strengths and weaknesses?", category = "GENERAL", difficulty = "EASY", expectedKeyPoints = listOf("Self-awareness", "Improvement plan")),
            InterviewQuestion(text = "Where do you see yourself in five years?", category = "GENERAL", difficulty = "MEDIUM", expectedKeyPoints = listOf("Growth", "Ambition", "Alignment"))
        )
    }
}
