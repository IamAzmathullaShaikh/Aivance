package com.bangersoul.aivance.feature.dashboard.data

import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.database.dao.TrackerDao
import com.bangersoul.aivance.feature.dashboard.domain.DashboardData
import com.bangersoul.aivance.feature.dashboard.domain.DashboardRepository
import com.bangersoul.aivance.feature.dashboard.domain.RecentActivity
import com.bangersoul.aivance.feature.dashboard.domain.ResumeStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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

        DashboardData(
            profileCompletion = 85,
            resumeStatus = ResumeStatus(
                fileName = "Resume",
                uploadedDate = LocalDate.now()
            ),
            atsScore = latestAts?.score ?: 0,
            activeApplications = applications.count {
                it.application.status.lowercase() != "rejected" &&
                    it.application.status.lowercase() != "closed"
            },
            interviewPrepStatus = "Ready to start",
            jobRecommendations = emptyList(),
            recentActivity = applications.take(3).map {
                RecentActivity(
                    id = it.application.id.toString(),
                    description = "Applied to position",
                    date = Instant.ofEpochMilli(it.application.dateApplied)
                        .atZone(ZoneId.systemDefault()).toLocalDate()
                )
            }
        )
    }
}
