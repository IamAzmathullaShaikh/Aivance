package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.*
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.AnalyticsDao
import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.domain.analytics.*
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import com.bangersoul.aivance.core.domain.repository.ApplicationWorkflowRepository
import com.bangersoul.aivance.core.domain.repository.crm.RecruiterIntelligenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val atsDao: AtsDao,
    private val workflowRepository: ApplicationWorkflowRepository,
    private val recruiterRepository: RecruiterIntelligenceRepository,
    private val userRepository: com.bangersoul.aivance.core.domain.repository.UserRepository,
    private val interviewRepository: com.bangersoul.aivance.core.domain.repository.InterviewRepository,
    private val kpiEngine: KPIEngine,
    private val scoreEngine: CareerScoreEngine,
    private val intelEngine: CareerIntelligenceEngine,
    private val forecastEngine: CareerForecastEngine,
    private val recommendationEngine: RecommendationEngine
) : AnalyticsRepository {

    override fun getSnapshots(): Flow<CoreResult<List<AnalyticsSnapshot>>> {
        return analyticsDao.getSnapshots().map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
    }

    override suspend fun createSnapshot(): CoreResult<Long> = runCatchingCore {
        val apps = workflowRepository.getApplications().firstOrNull()?.getOrNull() ?: emptyList()
        val sessions = interviewRepository.getSessions().firstOrNull()?.getOrNull() ?: emptyList()
        val atsResults = atsDao.getAtsResults().firstOrNull() ?: emptyList()
        val reports = atsResults.map { entity ->
            AtsReport(
                resumeVersionId = entity.resumeId,
                jobDescriptionId = 0,
                overallScore = entity.score,
                matchPercentage = entity.score
            )
        }
        val readiness = if (sessions.isNotEmpty()) {
            sessions.mapNotNull { it.feedback?.overallScore }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 75
        } else 75

        val recruiters = apps.flatMap { app ->
            recruiterRepository.getRecruitersForCompany(app.jobId).firstOrNull()?.getOrNull() ?: emptyList()
        }.distinctBy { it.id }

        val interviewRate = kpiEngine.calculateInterviewRate(apps)
        val scoreBreakdown = scoreEngine.calculateCompositeScore(reports, recruiters, apps.size, readiness)

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

    override fun getCareerIntelligence(): Flow<CoreResult<CareerIntelligence>> {
        return kotlinx.coroutines.flow.combine(
            atsDao.getAtsResults(),
            workflowRepository.getApplications(),
            userRepository.getProfile(),
            interviewRepository.getSessions(),
            analyticsDao.getSnapshots()
        ) { atsResults, appsRes, profileRes, interviewRes, snapshots ->
            runCatchingCore {
                val apps = appsRes.getOrNull() ?: emptyList()
                val profile = profileRes.getOrNull()
                val sessions = interviewRes.getOrNull() ?: emptyList()
                val recruiters = apps.flatMap { app ->
                    recruiterRepository.getRecruitersForCompany(app.jobId).firstOrNull()?.getOrNull() ?: emptyList()
                }.distinctBy { it.id }

                val reports = atsResults.map { entity ->
                    AtsReport(
                        resumeVersionId = entity.resumeId,
                        jobDescriptionId = 0,
                        overallScore = entity.score,
                        matchPercentage = entity.score
                    )
                }

                val readiness = if (sessions.isNotEmpty()) {
                    sessions.mapNotNull { it.feedback?.overallScore }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 75
                } else 75

                intelEngine.calculateIntelligence(
                    latestAtsReports = reports,
                    recruiters = recruiters,
                    applications = apps,
                    interviewReadiness = readiness
                ).copy(
                    weeklyReview = if (apps.isNotEmpty() || sessions.isNotEmpty()) {
                        "Hey ${profile?.fullName?.substringBefore(" ") ?: ""}, this week you applied to ${apps.size} roles, engaged with ${recruiters.size} recruiters, and completed ${sessions.size} mock interview sessions."
                    } else null
                )
            }
        }
    }

    override suspend fun runSimulation(ats: Int?, readiness: Int?): CoreResult<CareerIntelligence> = runCatchingCore {
        val current = getCareerIntelligence().first().getOrNull() ?: throw Exception("No intelligence data")
        forecastEngine.simulate(current, ats, readiness)
    }
}
