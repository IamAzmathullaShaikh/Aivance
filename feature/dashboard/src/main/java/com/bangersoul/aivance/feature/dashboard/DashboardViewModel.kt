package com.bangersoul.aivance.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.feature.dashboard.domain.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _effects = Channel<DashboardUiEffect>(Channel.BUFFERED)
    val effects: Flow<DashboardUiEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<DashboardUiState> = dashboardRepository.getDashboardData()
        .map { data: com.bangersoul.aivance.feature.dashboard.domain.DashboardData ->
            DashboardUiState.Success(
                dashboardData = data,
                recentAtsScore = data.atsScore,
                activeApplicationCount = data.activeApplications,
                upcomingInterviews = 0,
                profileCompletionPercent = data.profileCompletion / 100f,
                isRefreshing = false
            ) as DashboardUiState
        }
        .onStart { emit(DashboardUiState.Loading) }
        .catch { e ->
            emit(DashboardUiState.Error(
                message = e.message ?: "Failed to load dashboard",
                isOffline = e is java.net.UnknownHostException
            ))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState.Loading
        )

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            DashboardUiEvent.Refresh -> trackAndRefresh()
            DashboardUiEvent.Retry -> trackAndRefresh()
            DashboardUiEvent.NavigateToResume -> sendEffect(DashboardUiEffect.NavigateTo("resume"))
            DashboardUiEvent.NavigateToCoverLetter -> sendEffect(DashboardUiEffect.NavigateTo("cover_letter"))
            DashboardUiEvent.NavigateToInterview -> sendEffect(DashboardUiEffect.NavigateTo("interview"))
            DashboardUiEvent.NavigateToJobs -> sendEffect(DashboardUiEffect.NavigateTo("jobs"))
            DashboardUiEvent.NavigateToTracker -> sendEffect(DashboardUiEffect.NavigateTo("tracker"))
            DashboardUiEvent.NavigateToSettings -> sendEffect(DashboardUiEffect.OpenSettings)
        }
    }

    private fun trackAndRefresh() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "dashboard_refresh"))
        }
    }

    private fun sendEffect(effect: DashboardUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }

    fun trackEvent(name: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = name))
        }
    }
}
