package com.bangersoul.aivance.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val userName: String = "",
        val hasUnreadNotifications: Boolean = false,
        val quickActions: List<QuickAction> = QuickAction.entries,
        val greetingMessage: String = getGreeting()
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

enum class QuickAction {
    AnalyzeResume, GenerateCoverLetter, PracticeInterview, SearchJobs, ViewTracker, ViewRoadmap
}

fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }
}

sealed interface HomeUiEvent {
    data object NavigateToDashboard : HomeUiEvent
    data class PerformQuickAction(val action: QuickAction) : HomeUiEvent
    data object ShowNotifications : HomeUiEvent
}

sealed interface HomeUiEffect {
    data class Navigate(val route: String) : HomeUiEffect
    data class ShowSnackbar(val message: String) : HomeUiEffect
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effects = Channel<HomeUiEffect>(Channel.BUFFERED)
    val effects: Flow<HomeUiEffect> = _effects.receiveAsFlow()

    init { _uiState.value = HomeUiState.Success() }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.NavigateToDashboard -> sendEffect(HomeUiEffect.Navigate("dashboard"))
            is HomeUiEvent.PerformQuickAction -> handleQuickAction(event.action)
            HomeUiEvent.ShowNotifications -> sendEffect(HomeUiEffect.Navigate("notifications"))
        }
    }

    private fun handleQuickAction(action: QuickAction) {
        val route = when (action) {
            QuickAction.AnalyzeResume -> "resume"
            QuickAction.GenerateCoverLetter -> "cover_letter"
            QuickAction.PracticeInterview -> "interview"
            QuickAction.SearchJobs -> "jobs"
            QuickAction.ViewTracker -> "tracker"
            QuickAction.ViewRoadmap -> "roadmap"
        }
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "quick_action_${action.name}"))
            _effects.send(HomeUiEffect.Navigate(route))
        }
    }

    private fun sendEffect(effect: HomeUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
