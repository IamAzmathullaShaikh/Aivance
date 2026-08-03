package com.bangersoul.aivance.feature.dashboard.data

import com.bangersoul.aivance.core.database.dao.AnalyticsDao
import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.database.dao.ResumeDao
import com.bangersoul.aivance.core.database.dao.TrackerDao
import com.bangersoul.aivance.core.domain.analytics.CareerScoreEngine
import com.bangersoul.aivance.feature.dashboard.domain.CareerInsight
import com.bangersoul.aivance.feature.dashboard.domain.DashboardData
import com.bangersoul.aivance.feature.dashboard.domain.DashboardRepository
import com.bangersoul.aivance.feature.dashboard.domain.DashboardTask
import com.bangersoul.aivance.feature.dashboard.domain.JobRecommendation
import com.bangersoul.aivance.feature.dashboard.domain.RecentActivity
import com.bangersoul.aivance.feature.dashboard.domain.ResumeStatus
import com.bangersoul.aivance.feature.dashboard.domain.UpcomingInterview
import com.bangersoul.aivance.feature.dashboard.domain.WeeklyGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val trackerDao: TrackerDao,
    private val atsDao: AtsDao,
    private val resumeDao: ResumeDao,
    private val analyticsDao: AnalyticsDao,
    private val careerScoreEngine: CareerScoreEngine
) : DashboardRepository {

    override fun getDashboardData(): Flow<DashboardData> = combine(
        trackerDao.getApplications(),
        atsDao.getAtsResults(),
        resumeDao.getResumes(),
        analyticsDao.getSnapshots(),
        analyticsDao.getActiveRecommendations()
    ) { applications, atsResults, resumes, snapshots, recommendations ->
        val latestAts = atsResults.firstOrNull()
        val active = applications.filter {
            !it.application.status.equals("REJECTED", ignoreCase = true) &&
                !it.application.status.equals("CLOSED", ignoreCase = true)
        }

        // Pipeline progress grouped by status
        val pipelineProgress = active
            .groupBy { it.application.status.uppercase().ifBlank { "SAVED" } }
            .mapValues { it.value.size }

        // Upcoming interviews — applications in interview stages
        val interviews = active
            .filter { it.application.status.contains("INTERVIEW", ignoreCase = true) }
            .take(3)
            .map { entry ->
                UpcomingInterview(
                    id = entry.application.id.toString(),
                    company = entry.job.company.name,
                    role = entry.job.job.title,
                    dateTime = Instant.ofEpochMilli(entry.application.dateApplied)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("EEE HH:mm"))
                )
            }

        // Tasks derived from active pipeline
        val tasks = buildList {
            if (active.any { it.application.status.contains("APPLIED", ignoreCase = true) }) {
                add(DashboardTask(id = "followup", title = "Follow up on recent applications", priority = "HIGH"))
            }
            if (active.any { it.application.status.contains("INTERVIEW", ignoreCase = true) }) {
                add(DashboardTask(id = "interview-prep", title = "Prepare for upcoming interviews", priority = "HIGH"))
            }
            if (pipelineProgress.isEmpty()) {
                add(DashboardTask(id = "first-app", title = "Track your first application", priority = "MEDIUM"))
            }
        }.take(4)

        // Weekly goal: applications applied in the last 7 days
        val weekStart = Instant.now().minusSeconds(7 * 24 * 60 * 60).toEpochMilli()
        val appliedThisWeek = applications.count { it.application.dateApplied >= weekStart }

        val atsScore = latestAts?.score ?: 0

        // Career score: prefer the analytics snapshot (computed by the engine with
        // full context), otherwise compute a composite from real ATS + pipeline data.
        val snapshotScore = snapshots.firstOrNull()?.careerScore
        val compositeScore = careerScoreEngine.calculateCompositeScore(
            latestAtsReports = atsResults.map { legacy ->
                com.bangersoul.aivance.core.common.model.AtsReport(
                    resumeVersionId = legacy.resumeId,
                    jobDescriptionId = 0,
                    overallScore = legacy.score,
                    matchPercentage = legacy.score,
                    matchedKeywords = legacy.matchedKeywords.split(",").filter { it.isNotBlank() },
                    missingKeywords = legacy.missingKeywords.split(",").filter { it.isNotBlank() }
                )
            },
            recruiters = emptyList(),
            applicationCount = active.size,
            interviewReadiness = if (interviews.isNotEmpty()) 80 else 0 // Mock readiness
        )["OVERALL"] ?: 0
        val careerScore = snapshotScore ?: compositeScore

        // Real resume status from the uploaded resume (if any)
        val latestResume = resumes.firstOrNull()
        val resumeStatus = ResumeStatus(
            fileName = latestResume?.fileName ?: "No resume yet",
            uploadedDate = latestResume?.let {
                Instant.ofEpochMilli(it.lastModified).atZone(ZoneId.systemDefault()).toLocalDate()
            } ?: LocalDate.now()
        )

        // Profile completion from real signals (no hardcoded placeholder)
        val profileCompletion = listOf(
            if (latestResume != null) 30 else 0,          // resume uploaded
            if (atsScore > 0) 25 else 0,                   // ATS analyzed
            if (active.isNotEmpty()) 20 else 0,            // pipeline active
            if (recommendations.isNotEmpty()) 15 else 0,   // recommendations generated
            if (snapshots.isNotEmpty()) 10 else 0          // analytics snapshot exists
        ).sum().coerceIn(0, 100)

        // AI-generated recommendations surfaced from the analytics subsystem
        val jobRecommendations = recommendations.map { rec ->
            JobRecommendation(
                id = rec.id.toString(),
                title = rec.title,
                company = rec.category.replaceFirstChar { it.uppercase() }
            )
        }

        val insights = buildList {
            if (careerScore > 0) {
                add(
                    CareerInsight(
                        id = "career",
                        text = "Your career score is $careerScore — driven by ATS readiness, active applications, and engagement."
                    )
                )
            }
            if (atsScore > 0) {
                add(
                    CareerInsight(
                        id = "ats",
                        text = when {
                            atsScore >= 80 -> "Your ATS score is strong — you're ready to apply with confidence."
                            atsScore >= 60 -> "Your ATS score is $atsScore% — tailoring keywords could unlock more interviews."
                            else -> "Upload and analyze your resume against a target role to boost your match."
                        }
                    )
                )
            }
            if (active.isNotEmpty()) {
                add(
                    CareerInsight(
                        id = "pipeline",
                        text = "You have ${active.size} active applications across ${pipelineProgress.size} stages."
                    )
                )
            }
            if (jobRecommendations.isNotEmpty()) {
                add(
                    CareerInsight(
                        id = "recs",
                        text = "${jobRecommendations.size} AI-generated opportunities are waiting for you."
                    )
                )
            }
        }

        DashboardData(
            profileCompletion = profileCompletion,
            resumeStatus = resumeStatus,
            atsScore = atsScore,
            activeApplications = active.size,
            interviewPrepStatus = if (interviews.isNotEmpty()) "Interviews scheduled" else "Ready to start",
            jobRecommendations = jobRecommendations,
            recentActivity = applications.take(3).map {
                RecentActivity(
                    id = it.application.id.toString(),
                    description = "Applied to ${it.job.job.title} at ${it.job.company.name}",
                    date = Instant.ofEpochMilli(it.application.dateApplied)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                )
            },
            upcomingInterviews = interviews,
            pipelineProgress = pipelineProgress,
            tasks = tasks,
            weeklyGoals = if (appliedThisWeek > 0 || active.isNotEmpty()) {
                listOf(
                    WeeklyGoal(
                        id = "applications",
                        title = "Applications this week",
                        target = 5,
                        current = appliedThisWeek,
                        unit = "applications"
                    )
                )
            } else {
                emptyList()
            },
            insights = insights,
            careerScore = careerScore
        )
    }
}
