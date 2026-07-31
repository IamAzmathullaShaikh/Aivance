package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.*
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.AnalyticsDao
import com.bangersoul.aivance.core.domain.analytics.CareerScoreEngine
import com.bangersoul.aivance.core.domain.analytics.KPIEngine
import com.bangersoul.aivance.core.domain.analytics.RecommendationEngine
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import com.bangersoul.aivance.core.domain.repository.ApplicationWorkflowRepository
import com.bangersoul.aivance.core.domain.repository.crm.RecruiterIntelligenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val workflowRepository: ApplicationWorkflowRepository,
    private val recruiterRepository: RecruiterIntelligenceRepository,
    private val kpiEngine: KPIEngine,
    private val scoreEngine: CareerScoreEngine,
    private val recommendationEngine: RecommendationEngine
) : AnalyticsRepository {

    override fun getSnapshots(): Flow<CoreResult<List<AnalyticsSnapshot>>> {
        return analyticsDao.getSnapshots().map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
    }

    override suspend fun createSnapshot(): CoreResult<Long> = runCatchingCore {
        val apps = workflowRepository.getApplications().firstOrNull()?.getOrNull() ?: emptyList()
        // Aggregating all recruiters from all companies
        val recruiters = apps.flatMap { app ->
            recruiterRepository.getRecruitersForCompany(app.jobId).firstOrNull()?.getOrNull() ?: emptyList()
        }.distinctBy { it.id }

        val interviewRate = kpiEngine.calculateInterviewRate(apps)
        val scoreBreakdown = scoreEngine.calculateCompositeScore(emptyList(), recruiters, apps.size)

        val snapshot = AnalyticsSnapshot(
            kpis = mapOf("interview_rate" to interviewRate),
            careerScore = scoreBreakdown["OVERALL"] ?: 0,
            dimensionScores = scoreBreakdown
        )

        analyticsDao.insertSnapshot(snapshot.toEntity())
    }

    override fun getActiveRecommendations(): Flow<CoreResult<List<CareerRecommendation>>> {
        return analyticsDao.getActiveRecommendations().map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
    }

    override suspend fun refreshRecommendations(): CoreResult<Unit> = runCatchingCore {
        val summary = "System generated snapshot review"
        val result = recommendationEngine.generateRecommendations(summary)
        result.getOrNull()?.forEach { recommendation ->
            analyticsDao.insertRecommendation(recommendation.toEntity())
        }
    }

    override suspend fun dismissRecommendation(id: Long): CoreResult<Unit> = runCatchingCore {
        analyticsDao.dismissRecommendation(id)
    }

    override fun getGoals(): Flow<CoreResult<List<CareerGoal>>> {
        return analyticsDao.getGoals().map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
    }

    override suspend fun saveGoal(goal: CareerGoal): CoreResult<Long> = runCatchingCore {
        analyticsDao.insertGoal(goal.toEntity())
    }

    override suspend fun updateGoalProgress(id: Long, progress: Double): CoreResult<Unit> = runCatchingCore {
        analyticsDao.updateGoalProgress(id, progress)
    }
}
