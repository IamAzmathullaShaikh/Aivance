package com.bangersoul.aivance.feature.dashboard

sealed interface DashboardUiEvent {
    data object Refresh : DashboardUiEvent
    data object NavigateToResume : DashboardUiEvent
    data object NavigateToCoverLetter : DashboardUiEvent
    data object NavigateToInterview : DashboardUiEvent
    data object NavigateToJobs : DashboardUiEvent
    data object NavigateToTracker : DashboardUiEvent
    data object NavigateToSettings : DashboardUiEvent
    data object Retry : DashboardUiEvent
}
