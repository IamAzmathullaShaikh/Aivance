package com.bangersoul.aivance.core.domain.engine

import com.bangersoul.aivance.core.common.model.*
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.domain.repository.*
import com.bangersoul.aivance.sdk.core.ProviderStatus
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareerStateEngine @Inject constructor(
    private val userRepository: UserRepository,
    private val resumeRepository: ResumeRepository,
    private val workflowRepository: ApplicationWorkflowRepository,
    private val analyticsRepository: AnalyticsRepository,
    private val providerManager: ProviderManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val state: StateFlow<CareerState> = combine(
        combine(
            userRepository.getProfile(),
            resumeRepository.getResumes(),
            workflowRepository.getApplications(),
            ::Triple
        ),
        combine(
            analyticsRepository.getSnapshots(),
            analyticsRepository.getActiveRecommendations(),
            analyticsRepository.getCareerIntelligence(),
            ::Triple
        ),
        providerManager.providerStatuses
    ) { core, intel, providerStatuses ->
        val (profileRes, resumesRes, appsRes) = core
        val (snapshotsRes, recsRes, intelHubRes) = intel
        val profile = profileRes.getOrNull()
        val resumes = resumesRes.getOrNull() ?: emptyList()
        val applications = appsRes.getOrNull() ?: emptyList()
        val latestSnapshot = snapshotsRes.getOrNull()?.firstOrNull()
        val recommendations = recsRes.getOrNull() ?: emptyList()

        val activeApps = applications.filter { it.status == "ACTIVE" }
        val interviews = activeApps.filter { it.currentStageId.contains("INTERVIEW", ignoreCase = true) }

        val latestResume = resumes.firstOrNull()

        val lifecycleStage = determineLifecycleStage(resumes, activeApps, providerStatuses)

        CareerState(
            profile = ProfileState(
                name = profile?.fullName ?: "",
                targetRole = profile?.targetRole ?: "",
                skills = profile?.skills ?: emptyList(),
                completionPercentage = calculateCompletion(resumes, activeApps, latestSnapshot),
                workPreference = profile?.workPreference ?: "REMOTE",
                salaryExpectation = profile?.salaryExpectation ?: "",
                visaRequired = profile?.visaRequired ?: false
            ),
            intelligence = IntelligenceState(
                latestResumeId = latestResume?.id,
                atsScore = latestSnapshot?.dimensionScores?.get("ATS_READINESS") ?: 0,
                totalResumes = resumes.size
            ),
            discovery = DiscoveryState(
                savedJobsCount = applications.count { it.currentStageId == "SAVED" }
            ),
            pipeline = PipelineState(
                activeApplications = activeApps.size,
                upcomingInterviews = interviews.map {
                    UpcomingInterviewShort(
                        id = it.id.toString(),
                        company = it.job?.company ?: "Unknown",
                        role = it.job?.title ?: "Unknown",
                        dateTime = it.dateApplied?.toString() ?: ""
                    )
                },
                pipelineDistribution = activeApps.groupBy { it.currentStageId }.mapValues { it.value.size }
            ),
            growth = GrowthState(
                careerScore = latestSnapshot?.careerScore ?: 0,
                weeklyApplicationCount = 0
            ),
            recommendations = recommendations,
            nextBestAction = recommendations.firstOrNull(),
            lifecycleStage = lifecycleStage,
            intelligenceHub = intelHubRes.getOrNull()
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = CareerState()
    )

    private fun determineLifecycleStage(
        resumes: List<Resume>,
        apps: List<Application>,
        providerStatuses: Map<String, ProviderStatus>
    ): CareerLifecycleStage {
        val hasAI = providerStatuses.values.any { it == ProviderStatus.Healthy || it == ProviderStatus.Ready || it == ProviderStatus.Active }
        if (!hasAI) return CareerLifecycleStage.ONBOARDING
        if (resumes.isEmpty()) return CareerLifecycleStage.PREPARING
        if (apps.isEmpty()) return CareerLifecycleStage.EXPLORING
        if (apps.any { it.currentStageId.contains("INTERVIEW", ignoreCase = true) }) return CareerLifecycleStage.INTERVIEWING
        return CareerLifecycleStage.APPLYING
    }

    private fun calculateCompletion(resumes: List<Resume>, apps: List<Application>, snapshot: AnalyticsSnapshot?): Int {
        var score = 0
        if (resumes.isNotEmpty()) score += 30
        if (apps.isNotEmpty()) score += 30
        if (snapshot != null) score += 40
        return score.coerceAtMost(100)
    }
}
