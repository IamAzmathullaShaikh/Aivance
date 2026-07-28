package com.bangersoul.aivance.feature.dashboard.domain

import java.time.LocalDate

data class DashboardData(
    val profileCompletion: Int,
    val resumeStatus: ResumeStatus,
    val atsScore: Int,
    val activeApplications: Int,
    val interviewPrepStatus: String,
    val jobRecommendations: List<JobRecommendation> = emptyList(),
    val recentActivity: List<RecentActivity> = emptyList()
)

data class ResumeStatus(
    val fileName: String,
    val uploadedDate: LocalDate
)

data class JobRecommendation(
    val id: String,
    val title: String,
    val company: String
)

data class RecentActivity(
    val id: String,
    val description: String,
    val date: LocalDate
)
