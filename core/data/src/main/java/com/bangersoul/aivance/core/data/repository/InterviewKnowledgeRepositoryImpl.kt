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
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterviewKnowledgeRepositoryImpl @Inject constructor(
    private val interviewDao: InterviewDao,
    private val aiRepository: AiRepository
) : InterviewKnowledgeRepository {

    override fun getCommonQuestions(category: String): Flow<CoreResult<List<InterviewQuestion>>> {
        // Mocking session ID 0 for generic library questions
        return interviewDao.getQuestionsForSession(0L).map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
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
}
