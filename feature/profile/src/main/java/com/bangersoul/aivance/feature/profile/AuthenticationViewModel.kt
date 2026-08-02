package com.bangersoul.aivance.feature.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.database.dao.UserDao
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.GetProviderHealthUseCase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val trackEventUseCase: TrackEventUseCase,
    private val userDao: UserDao,
    @ApplicationContext private val appContext: Context
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
            // A completed onboarding (with any AI provider) is equivalent to being
            // authenticated; the legacy Gemini key check alone locked users out of the
            // app after onboarding with Ollama or other non-Gemini providers.
            val isOnboarded = prefs?.onboardingCompleted == true
            val hasKey = !prefs?.geminiApiKey.isNullOrBlank()

            // Google-authenticated sessions must still have a live Firebase user.
            // If the DataStore session points at a Google account but Firebase has
            // been signed out (server-side, another device, token expiry), force
            // re-authentication instead of silently trusting the local flag.
            val needsReauth = sessionNeedsReauth(prefs?.userId)

            val authenticated = (isOnboarded || hasKey) && needsReauth != true
            _uiState.value = if (authenticated) AuthenticationUiState.Authenticated
            else AuthenticationUiState.Unauthenticated
        }
    }

    /**
     * @return true when the stored session belongs to a Google account whose
     * Firebase session is no longer valid, false when the session is valid, and
     * null when Firebase isn't configured or the session can't be classified.
     */
    private suspend fun sessionNeedsReauth(sessionUserId: String?): Boolean? {
        if (sessionUserId.isNullOrBlank()) return null
        return try {
            if (FirebaseApp.getApps(appContext).isEmpty()) return null
            if (FirebaseAuth.getInstance().currentUser != null) return false
            // No live Firebase user: only force re-auth when the stored session
            // was created through Google Sign-In (email sessions stay local).
            userDao.getUserById(sessionUserId)?.googleId?.isNotBlank() == true
        } catch (e: Exception) {
            null
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
            // Reset the onboarding gate too — checkAuthentication() treats
            // onboardingCompleted == true as Authenticated, so a sign-out that
            // only cleared the API key would silently log the user back in on
            // the next cold start.
            userPreferencesRepository.updateOnboardingCompleted(false)
            userPreferencesRepository.clearSession()
            // Best-effort Firebase sign-out so the Google account isn't silently
            // re-authenticated on the next cold start (local-only; safe to skip
            // when Firebase isn't configured).
            try {
                if (FirebaseApp.getApps(appContext).isNotEmpty()) {
                    FirebaseAuth.getInstance().signOut()
                }
            } catch (e: Exception) {
                // Local session reset above is sufficient when Firebase is absent.
            }
            _uiState.value = AuthenticationUiState.Unauthenticated
            _effects.send(AuthenticationUiEffect.NavigateToOnboarding)
            _effects.send(AuthenticationUiEffect.ShowSnackbar("Logged out"))
        }
    }
}
