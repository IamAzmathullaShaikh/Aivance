package com.bangersoul.aivance.feature.dashboard.data

import com.bangersoul.aivance.feature.dashboard.domain.DashboardData
import com.bangersoul.aivance.feature.dashboard.domain.DashboardRepository
import com.bangersoul.aivance.feature.dashboard.domain.ResumeStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject

class FakeDashboardRepository @Inject constructor() : DashboardRepository {
    override fun getDashboardData(): Flow<DashboardData> = flow {
        // Simulate network delay
        delay(1500)
        emit(
            DashboardData(
                profileCompletion = 72,
                resumeStatus = ResumeStatus(
                    fileName = "Resume_Software_Engineer_2026.pdf",
                    uploadedDate = LocalDate.now().minusDays(2)
                ),
                atsScore = 85,
                activeApplications = 12,
                interviewPrepStatus = "8/10 Modules Completed",
                jobRecommendations = emptyList(), // Placeholder
                recentActivity = emptyList() // Placeholder
            )
        )
    }
}
