package com.bangersoul.aivance.feature.dashboard

import com.bangersoul.aivance.feature.dashboard.domain.DashboardData

sealed interface DashboardUiState {
    data object Loading : DashboardUiState

    data class Success(
        val dashboardData: DashboardData? = null,
        val recentAtsScore: Int? = null,
        val activeApplicationCount: Int = 0,
        val upcomingInterviews: Int = 0,
        val profileCompletionPercent: Float = 0f,
        val isRefreshing: Boolean = false
    ) : DashboardUiState

    data object Empty : DashboardUiState

    data class Error(
        val message: String? = null,
        val isOffline: Boolean = false
    ) : DashboardUiState
}
