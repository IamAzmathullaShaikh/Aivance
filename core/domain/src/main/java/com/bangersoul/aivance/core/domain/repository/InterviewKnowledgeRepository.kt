package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.model.InterviewQuestion
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface InterviewKnowledgeRepository {
    fun getCommonQuestions(category: String): Flow<CoreResult<List<InterviewQuestion>>>
    fun getFavoriteQuestions(): Flow<CoreResult<List<InterviewQuestion>>>
    suspend fun saveQuestionToLibrary(question: InterviewQuestion): CoreResult<Long>
    suspend fun getIdealAnswer(questionId: Long): CoreResult<String>
    suspend fun toggleFavorite(questionId: Long): CoreResult<Unit>
}
