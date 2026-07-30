package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OnboardingUiState {
    data object Welcome : OnboardingUiState
    data object Permissions : OnboardingUiState
    data class ApiKeySetup(val apiKey: String = "") : OnboardingUiState
    data object ProfileSetup : OnboardingUiState
    data class ProviderSelection(val selectedProvider: String = "Gemini") : OnboardingUiState
    data object Complete : OnboardingUiState
}

sealed interface OnboardingUiEvent {
    data object NextStep : OnboardingUiEvent
    data object Skip : OnboardingUiEvent
    data class SetApiKey(val key: String) : OnboardingUiEvent
    data class SelectProvider(val provider: String) : OnboardingUiEvent
    data object CompleteOnboarding : OnboardingUiEvent
}

sealed interface OnboardingUiEffect {
    data class ShowSnackbar(val message: String) : OnboardingUiEffect
    data object NavigateToHome : OnboardingUiEffect
    data object RequestNotificationPermission : OnboardingUiEffect
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Welcome)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _effects = Channel<OnboardingUiEffect>(Channel.BUFFERED)
    val effects: Flow<OnboardingUiEffect> = _effects.receiveAsFlow()

    private val steps = listOf(
        OnboardingUiState.Welcome,
        OnboardingUiState.Permissions,
        OnboardingUiState.ProviderSelection(),
        OnboardingUiState.ApiKeySetup(),
        OnboardingUiState.ProfileSetup,
        OnboardingUiState.Complete
    )

    private var currentStepIndex = 0

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            OnboardingUiEvent.NextStep -> nextStep()
            OnboardingUiEvent.Skip -> skipOnboarding()
            is OnboardingUiEvent.SetApiKey -> {
                _uiState.value = OnboardingUiState.ApiKeySetup(apiKey = event.key)
            }
            is OnboardingUiEvent.SelectProvider -> {
                _uiState.value = OnboardingUiState.ProviderSelection(selectedProvider = event.provider)
            }
            OnboardingUiEvent.CompleteOnboarding -> completeOnboarding()
        }
    }

    private fun nextStep() {
        val currentState = _uiState.value
        when (currentState) {
            is OnboardingUiState.Welcome -> {
                _uiState.value = OnboardingUiState.Permissions
                viewModelScope.launch { _effects.send(OnboardingUiEffect.RequestNotificationPermission) }
            }
            is OnboardingUiState.Permissions -> {
                _uiState.value = OnboardingUiState.ProviderSelection()
            }
            is OnboardingUiState.ProviderSelection -> {
                _uiState.value = OnboardingUiState.ApiKeySetup()
            }
            is OnboardingUiState.ApiKeySetup -> {
                _uiState.value = OnboardingUiState.ProfileSetup
            }
            is OnboardingUiState.ProfileSetup -> {
                completeOnboarding()
            }
            is OnboardingUiState.Complete -> {
                completeOnboarding()
            }
        }
    }

    private fun completeOnboarding() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "onboarding_complete"))
            val state = _uiState.value
            if (state is OnboardingUiState.ApiKeySetup && state.apiKey.isNotBlank()) {
                userPreferencesRepository.updateGeminiApiKey(state.apiKey)
            }
            _uiState.value = OnboardingUiState.Complete
            _effects.send(OnboardingUiEffect.NavigateToHome)
        }
    }

    private fun skipOnboarding() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "onboarding_skip"))
            _effects.send(OnboardingUiEffect.NavigateToHome)
        }
    }
}
