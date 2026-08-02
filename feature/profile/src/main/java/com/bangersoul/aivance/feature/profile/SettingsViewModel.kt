package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.ExportSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.LoadSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.ResetSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.SaveSettingsRequest
import com.bangersoul.aivance.core.domain.usecase.settings.SaveSettingsUseCase
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

data class AppSettings(
    val themeMode: String = "system",
    val dynamicColorEnabled: Boolean = true,
    val language: String = "en",
    val analyticsEnabled: Boolean = true,
    val localProcessingOnly: Boolean = false,
    val autoSave: Boolean = true,
    val jobAlertsEnabled: Boolean = true,
    val interviewRemindersEnabled: Boolean = true,
    val followUpRemindersEnabled: Boolean = true
)

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(
        val settings: AppSettings = AppSettings(),
        val isSaving: Boolean = false
    ) : SettingsUiState
    data class Error(val message: String) : SettingsUiState
}

sealed interface SettingsUiEvent {
    data class SetThemeMode(val mode: String) : SettingsUiEvent
    data class SetDynamicColor(val enabled: Boolean) : SettingsUiEvent
    data class SetLanguage(val language: String) : SettingsUiEvent
    data class SetAnalyticsEnabled(val enabled: Boolean) : SettingsUiEvent
    data class SetLocalProcessing(val enabled: Boolean) : SettingsUiEvent
    data class SetAutoSave(val enabled: Boolean) : SettingsUiEvent
    data class SetJobAlerts(val enabled: Boolean) : SettingsUiEvent
    data class SetInterviewReminders(val enabled: Boolean) : SettingsUiEvent
    data class SetFollowUpReminders(val enabled: Boolean) : SettingsUiEvent
    data object SaveSettings : SettingsUiEvent
    data object ExportSettings : SettingsUiEvent
    data object ResetSettings : SettingsUiEvent
    data object SignOut : SettingsUiEvent
}

sealed interface SettingsUiEffect {
    data class ShowSnackbar(val message: String) : SettingsUiEffect
    data class ExportResult(val path: String) : SettingsUiEffect
    data object ThemeChanged : SettingsUiEffect
    data object SignedOut : SettingsUiEffect
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val loadSettingsUseCase: LoadSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val exportSettingsUseCase: ExportSettingsUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase,
    private val trackEventUseCase: TrackEventUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<SettingsUiEffect>(Channel.BUFFERED)
    val effects: Flow<SettingsUiEffect> = _effects.receiveAsFlow()

    private var pendingChanges = AppSettings()

    init {
        loadSettings()
    }

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.SetThemeMode -> pendingChanges = pendingChanges.copy(themeMode = event.mode)
            is SettingsUiEvent.SetDynamicColor -> pendingChanges = pendingChanges.copy(dynamicColorEnabled = event.enabled)
            is SettingsUiEvent.SetLanguage -> setLanguage(event.language)
            is SettingsUiEvent.SetAnalyticsEnabled -> pendingChanges = pendingChanges.copy(analyticsEnabled = event.enabled)
            is SettingsUiEvent.SetLocalProcessing -> pendingChanges = pendingChanges.copy(localProcessingOnly = event.enabled)
            is SettingsUiEvent.SetAutoSave -> pendingChanges = pendingChanges.copy(autoSave = event.enabled)
            is SettingsUiEvent.SetJobAlerts -> setJobAlerts(event.enabled)
            is SettingsUiEvent.SetInterviewReminders -> setInterviewReminders(event.enabled)
            is SettingsUiEvent.SetFollowUpReminders -> setFollowUpReminders(event.enabled)
            SettingsUiEvent.SaveSettings -> save()
            SettingsUiEvent.ExportSettings -> export()
            SettingsUiEvent.ResetSettings -> reset()
            SettingsUiEvent.SignOut -> signOut()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            // Load persisted notification prefs + session state so the toggles
            // reflect reality instead of hardcoded defaults.
            val prefs = userPreferencesRepository.userPreferences.firstOrNull()
            pendingChanges = AppSettings(
                jobAlertsEnabled = prefs?.jobAlertsEnabled ?: true,
                interviewRemindersEnabled = prefs?.interviewRemindersEnabled ?: true,
                followUpRemindersEnabled = prefs?.followUpRemindersEnabled ?: true,
                language = prefs?.language ?: "en"
            )
            _uiState.value = SettingsUiState.Success(settings = pendingChanges)
        }
    }

    private fun setJobAlerts(enabled: Boolean) {
        pendingChanges = pendingChanges.copy(jobAlertsEnabled = enabled)
        viewModelScope.launch { userPreferencesRepository.updateJobAlertsEnabled(enabled) }
        refreshState()
    }

    private fun setInterviewReminders(enabled: Boolean) {
        pendingChanges = pendingChanges.copy(interviewRemindersEnabled = enabled)
        viewModelScope.launch { userPreferencesRepository.updateInterviewRemindersEnabled(enabled) }
        refreshState()
    }

    private fun setFollowUpReminders(enabled: Boolean) {
        pendingChanges = pendingChanges.copy(followUpRemindersEnabled = enabled)
        viewModelScope.launch { userPreferencesRepository.updateFollowUpRemindersEnabled(enabled) }
        refreshState()
    }

    private fun setLanguage(language: String) {
        pendingChanges = pendingChanges.copy(language = language)
        viewModelScope.launch {
            userPreferencesRepository.updateLanguage(language)
            trackEventUseCase(TrackEventRequest(eventName = "settings_language_$language"))
            _effects.send(SettingsUiEffect.ShowSnackbar("Language set to ${languageName(language)}"))
        }
        refreshState()
    }

    private fun languageName(code: String): String = when (code) {
        "en" -> "English"
        "hi" -> "हिन्दी (Hindi)"
        "es" -> "Español"
        "fr" -> "Français"
        "de" -> "Deutsch"
        "zh" -> "中文"
        "ja" -> "日本語"
        else -> code
    }

    private fun refreshState() {
        val current = _uiState.value
        if (current is SettingsUiState.Success) {
            _uiState.value = current.copy(settings = pendingChanges)
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "settings_sign_out"))
            userPreferencesRepository.clearSession()
            _effects.send(SettingsUiEffect.SignedOut)
            _effects.send(SettingsUiEffect.ShowSnackbar("Signed out"))
        }
    }

    private fun save() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "settings_save"))
            val currentState = _uiState.value
            if (currentState is SettingsUiState.Success) {
                _uiState.value = currentState.copy(isSaving = true)
                // SaveSettingsUseCase takes SaveSettingsRequest
                val request = SaveSettingsRequest()
                val result = saveSettingsUseCase(request)
                when (result) {
                    is Result.Success<*> -> {
                        _uiState.value = SettingsUiState.Success(settings = pendingChanges, isSaving = false)
                        _effects.send(SettingsUiEffect.ShowSnackbar("Settings saved"))
                        _effects.send(SettingsUiEffect.ThemeChanged)
                    }
                    is Result.Failure -> {
                        _uiState.value = currentState.copy(isSaving = false)
                        _effects.send(SettingsUiEffect.ShowSnackbar(result.error.message))
                    }
                }
            }
        }
    }

    private fun export() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "settings_export"))
            // ExportSettingsUseCase extends NoInputUseCase — no arguments
            val result = exportSettingsUseCase()
            when (result) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val path = (result as Result.Success<String>).data
                    _effects.send(SettingsUiEffect.ExportResult(path))
                    _effects.send(SettingsUiEffect.ShowSnackbar("Settings exported"))
                }
                is Result.Failure -> {
                    _effects.send(SettingsUiEffect.ShowSnackbar(result.error.message.ifBlank { "Export failed" }))
                }
            }
        }
    }

    private fun reset() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "settings_reset"))
            val result = resetSettingsUseCase()
            @Suppress("UNCHECKED_CAST")
            when (result) {
                is Result.Success<*> -> {
                    pendingChanges = AppSettings()
                    _uiState.value = SettingsUiState.Success(settings = AppSettings())
                    _effects.send(SettingsUiEffect.ShowSnackbar("Settings reset to defaults"))
                }
                is Result.Failure -> {
                    _effects.send(SettingsUiEffect.ShowSnackbar(result.error.message))
                }
            }
        }
    }
}
