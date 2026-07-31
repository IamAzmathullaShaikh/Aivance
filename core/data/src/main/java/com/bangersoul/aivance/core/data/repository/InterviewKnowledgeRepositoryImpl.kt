package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.InterviewQuestion
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.InterviewDao
import com.bangersoul.aivance.core.domain.repository.InterviewKnowledgeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterviewKnowledgeRepositoryImpl @Inject constructor(
    private val interviewDao: InterviewDao
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
        // Logic to fetch or generate ideal answer via AI
        "This is an ideal answer sample."
    }
}
