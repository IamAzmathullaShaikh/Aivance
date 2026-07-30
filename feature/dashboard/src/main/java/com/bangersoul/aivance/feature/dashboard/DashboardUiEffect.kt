package com.bangersoul.aivance.feature.dashboard

sealed interface DashboardUiEffect {
    data class ShowSnackbar(val message: String) : DashboardUiEffect
    data class NavigateTo(val route: String) : DashboardUiEffect
    data object OpenSettings : DashboardUiEffect
}
