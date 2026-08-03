package com.bangersoul.aivance.feature.dashboard

/**
 * Aggregated Career HQ state — every field is derived from real repositories,
 * never hardcoded.
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val greeting: String = "",              // "Good Morning, Azmath"
    val userDesignation: String = "",       // "Software Engineer at TCS"
    val careerScore: Int = 0,
    val atsScore: Int = 0,
    val activeApplications: Int = 0,
    val nextInterview: String? = null,      // "Fri 10:00"
    val savedJobs: Int = 0,
    val aiRecommendation: String? = null,
    val nextBestAction: com.bangersoul.aivance.core.domain.engine.NavigationIntent = com.bangersoul.aivance.core.domain.engine.NavigationIntent.None,
    val recentActivity: List<ActivityItem> = emptyList(),
    val error: String? = null
)

data class ActivityItem(
    val id: String,
    val description: String,
    val date: String
)
