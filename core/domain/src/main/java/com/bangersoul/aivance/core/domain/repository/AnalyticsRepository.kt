package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.model.AnalyticsSnapshot
import com.bangersoul.aivance.core.common.model.CareerGoal
import com.bangersoul.aivance.core.common.model.CareerRecommendation
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun getSnapshots(): Flow<CoreResult<List<AnalyticsSnapshot>>>
    suspend fun createSnapshot(): CoreResult<Long>

    fun getActiveRecommendations(): Flow<CoreResult<List<CareerRecommendation>>>
    suspend fun refreshRecommendations(): CoreResult<Unit>
    suspend fun dismissRecommendation(id: Long): CoreResult<Unit>

    fun getGoals(): Flow<CoreResult<List<CareerGoal>>>
    suspend fun saveGoal(goal: CareerGoal): CoreResult<Long>
    suspend fun updateGoalProgress(id: Long, progress: Double): CoreResult<Unit>
}
