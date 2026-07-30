package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.GetProviderHealthUseCase
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

sealed interface AuthenticationUiState {
    data object Idle : AuthenticationUiState
    data object Loading : AuthenticationUiState
    data object Authenticated : AuthenticationUiState
    data object Unauthenticated : AuthenticationUiState
    data class Error(val message: String) : AuthenticationUiState
}

sealed interface AuthenticationUiEvent {
    data class Login(val apiKey: String) : AuthenticationUiEvent
    data object Logout : AuthenticationUiEvent
    data object CheckAuth : AuthenticationUiEvent
}

sealed interface AuthenticationUiEffect {
    data class ShowSnackbar(val message: String) : AuthenticationUiEffect
    data object NavigateToHome : AuthenticationUiEffect
    data object NavigateToOnboarding : AuthenticationUiEffect
}

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getProviderHealthUseCase: GetProviderHealthUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthenticationUiState>(AuthenticationUiState.Loading)
    val uiState: StateFlow<AuthenticationUiState> = _uiState.asStateFlow()

    private val _effects = Channel<AuthenticationUiEffect>(Channel.BUFFERED)
    val effects: Flow<AuthenticationUiEffect> = _effects.receiveAsFlow()

    init {
        checkAuthentication()
    }

    fun onEvent(event: AuthenticationUiEvent) {
        when (event) {
            is AuthenticationUiEvent.Login -> login(event.apiKey)
            AuthenticationUiEvent.Logout -> logout()
            AuthenticationUiEvent.CheckAuth -> checkAuthentication()
        }
    }

    private fun checkAuthentication() {
        viewModelScope.launch {
            val prefs = userPreferencesRepository.userPreferences.firstOrNull()
            val hasKey = !prefs?.geminiApiKey.isNullOrBlank()
            _uiState.value = if (hasKey) AuthenticationUiState.Authenticated
            else AuthenticationUiState.Unauthenticated
        }
    }

    private fun login(apiKey: String) {
        if (apiKey.isBlank()) {
            _uiState.value = AuthenticationUiState.Error("API key is required")
            return
        }
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "auth_login"))
            _uiState.value = AuthenticationUiState.Loading

            try {
                userPreferencesRepository.updateGeminiApiKey(apiKey)
                _uiState.value = AuthenticationUiState.Authenticated
                _effects.send(AuthenticationUiEffect.NavigateToHome)
                _effects.send(AuthenticationUiEffect.ShowSnackbar("Authenticated successfully"))
            } catch (e: Exception) {
                _uiState.value = AuthenticationUiState.Error(e.message ?: "Authentication failed")
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "auth_logout"))
            userPreferencesRepository.updateGeminiApiKey("")
            _uiState.value = AuthenticationUiState.Unauthenticated
            _effects.send(AuthenticationUiEffect.NavigateToOnboarding)
            _effects.send(AuthenticationUiEffect.ShowSnackbar("Logged out"))
        }
    }
}
