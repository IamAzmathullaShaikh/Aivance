package com.bangersoul.aivance.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.user.LoadProfileUseCase
import com.bangersoul.aivance.feature.dashboard.domain.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val jobRepository: JobRepository,
    private val loadProfileUseCase: LoadProfileUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _effects = Channel<DashboardUiEffect>(Channel.BUFFERED)
    val effects: Flow<DashboardUiEffect> = _effects.receiveAsFlow()

    /** Re-subscribes the aggregate flow on refresh/retry so new data is pulled. */
    private val refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<DashboardUiState> = refreshTrigger
        .flatMapLatest {
            combine(
                dashboardRepository.getDashboardData(),
                jobRepository.getSavedJobs(),
                loadProfileUseCase.invoke()
            ) { data, savedJobsResult, profileResult ->
                val profile = (profileResult as? Result.Success)?.data
                DashboardUiState(
                    isLoading = false,
                    greeting = buildGreeting(profile),
                    userDesignation = profile?.targetRole.orEmpty(),
                    careerScore = data.careerScore,
                    atsScore = data.atsScore,
                    activeApplications = data.activeApplications,
                    nextInterview = data.upcomingInterviews.firstOrNull()?.dateTime,
                    savedJobs = (savedJobsResult as? Result.Success)?.data?.size ?: 0,
                    aiRecommendation = data.jobRecommendations.firstOrNull()?.let {
                        "${it.title} at ${it.company}"
                    },
                    recentActivity = data.recentActivity.take(5).map {
                        ActivityItem(id = it.id, description = it.description, date = it.date.toString())
                    }
                )
            }
        }
        .onStart { emit(DashboardUiState(isLoading = true)) }
        .catch { e ->
            emit(DashboardUiState(isLoading = false, error = e.message ?: "Failed to load dashboard"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(isLoading = true)
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
        refreshTrigger.value++
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "dashboard_refresh"))
        }
    }

    private fun buildGreeting(profile: UserProfile?): String {
        val firstName = profile?.fullName
            ?.trim()
            ?.substringBefore(' ')
            ?.takeIf { it.isNotBlank() }
        val timeGreeting = getGreeting()
            .split(' ')
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        return if (firstName != null) "$timeGreeting, $firstName" else timeGreeting
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
