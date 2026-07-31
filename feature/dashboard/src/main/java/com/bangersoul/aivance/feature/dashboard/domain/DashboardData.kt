package com.bangersoul.aivance.feature.dashboard.domain

import java.time.LocalDate

data class DashboardData(
    val profileCompletion: Int,
    val resumeStatus: ResumeStatus,
    val atsScore: Int,
    val activeApplications: Int,
    val interviewPrepStatus: String,
    val jobRecommendations: List<JobRecommendation> = emptyList(),
    val recentActivity: List<RecentActivity> = emptyList(),
    val upcomingInterviews: List<UpcomingInterview> = emptyList(),
    val pipelineProgress: Map<String, Int> = emptyMap(),
    val tasks: List<DashboardTask> = emptyList(),
    val weeklyGoals: List<WeeklyGoal> = emptyList(),
    val insights: List<CareerInsight> = emptyList()
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

data class UpcomingInterview(
    val id: String,
    val company: String,
    val role: String,
    val dateTime: String
)

data class DashboardTask(
    val id: String,
    val title: String,
    val priority: String = "MEDIUM",
    val isCompleted: Boolean = false
)

data class WeeklyGoal(
    val id: String,
    val title: String,
    val target: Int,
    val current: Int,
    val unit: String = "applications"
)

data class CareerInsight(
    val id: String,
    val text: String,
    val tone: String = "INFO"
)
