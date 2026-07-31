package com.bangersoul.aivance.feature.dashboard.data

import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.database.dao.TrackerDao
import com.bangersoul.aivance.feature.dashboard.domain.CareerInsight
import com.bangersoul.aivance.feature.dashboard.domain.DashboardData
import com.bangersoul.aivance.feature.dashboard.domain.DashboardRepository
import com.bangersoul.aivance.feature.dashboard.domain.DashboardTask
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
    private val atsDao: AtsDao
) : DashboardRepository {

    override fun getDashboardData(): Flow<DashboardData> = combine(
        trackerDao.getApplications(),
        atsDao.getAtsResults()
    ) { applications, atsResults ->
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
        val insights = buildList {
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
        }

        DashboardData(
            profileCompletion = 85,
            resumeStatus = ResumeStatus(
                fileName = "Resume",
                uploadedDate = LocalDate.now()
            ),
            atsScore = atsScore,
            activeApplications = active.size,
            interviewPrepStatus = "Ready to start",
            jobRecommendations = emptyList(),
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
            insights = insights
        )
    }
}
