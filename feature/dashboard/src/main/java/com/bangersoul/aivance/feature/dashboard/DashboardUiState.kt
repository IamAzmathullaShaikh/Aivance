package com.bangersoul.aivance.feature.dashboard

import com.bangersoul.aivance.feature.dashboard.domain.DashboardData

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    
    data class Success(
        val dashboardData: DashboardData
    ) : DashboardUiState
    
    object Empty : DashboardUiState
    
    data class Error(
        val message: String? = null
    ) : DashboardUiState
}
