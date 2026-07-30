package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.ExportSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.LoadSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.ResetSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.SaveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    val autoSave: Boolean = true
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
    data object SaveSettings : SettingsUiEvent
    data object ExportSettings : SettingsUiEvent
    data object ResetSettings : SettingsUiEvent
}

sealed interface SettingsUiEffect {
    data class ShowSnackbar(val message: String) : SettingsUiEffect
    data class ExportResult(val path: String) : SettingsUiEffect
    data object ThemeChanged : SettingsUiEffect
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val loadSettingsUseCase: LoadSettingsUseCase,
    private val saveSettingsUseCase: SaveSettingsUseCase,
    private val exportSettingsUseCase: ExportSettingsUseCase,
    private val resetSettingsUseCase: ResetSettingsUseCase,
    private val trackEventUseCase: TrackEventUseCase
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
            is SettingsUiEvent.SetLanguage -> pendingChanges = pendingChanges.copy(language = event.language)
            is SettingsUiEvent.SetAnalyticsEnabled -> pendingChanges = pendingChanges.copy(analyticsEnabled = event.enabled)
            is SettingsUiEvent.SetLocalProcessing -> pendingChanges = pendingChanges.copy(localProcessingOnly = event.enabled)
            is SettingsUiEvent.SetAutoSave -> pendingChanges = pendingChanges.copy(autoSave = event.enabled)
            SettingsUiEvent.SaveSettings -> save()
            SettingsUiEvent.ExportSettings -> export()
            SettingsUiEvent.ResetSettings -> reset()
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            val result = loadSettingsUseCase().firstOrNull()
            when (result) {
                is CoreResult.Success -> {
                    pendingChanges = result.data
                    _uiState.value = SettingsUiState.Success(settings = result.data)
                }
                is CoreResult.Failure -> {
                    _uiState.value = SettingsUiState.Error(result.error.message ?: "Failed to load settings")
                }
                null -> {
                    _uiState.value = SettingsUiState.Success()
                }
            }
        }
    }

    private fun save() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "settings_save"))
            val currentState = _uiState.value
            if (currentState is SettingsUiState.Success) {
                _uiState.value = currentState.copy(isSaving = true)
                val result = saveSettingsUseCase(pendingChanges).firstOrNull()
                when (result) {
                    is CoreResult.Success -> {
                        _uiState.value = SettingsUiState.Success(settings = pendingChanges, isSaving = false)
                        _effects.send(SettingsUiEffect.ShowSnackbar("Settings saved"))
                        _effects.send(SettingsUiEffect.ThemeChanged)
                    }
                    is CoreResult.Failure -> {
                        _uiState.value = currentState.copy(isSaving = false)
                        _effects.send(SettingsUiEffect.ShowSnackbar(
                            result.error.message ?: "Failed to save settings"
                        ))
                    }
                    null -> {
                        _uiState.value = currentState.copy(isSaving = false)
                    }
                }
            }
        }
    }

    private fun export() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "settings_export"))
            val result = exportSettingsUseCase("json").firstOrNull()
            when (result) {
                is CoreResult.Success -> {
                    _effects.send(SettingsUiEffect.ExportResult(result.data))
                    _effects.send(SettingsUiEffect.ShowSnackbar("Settings exported"))
                }
                is CoreResult.Failure -> {
                    _effects.send(SettingsUiEffect.ShowSnackbar("Export failed"))
                }
                null -> { /* noop */ }
            }
        }
    }

    private fun reset() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "settings_reset"))
            val result = resetSettingsUseCase().firstOrNull()
            when (result) {
                is CoreResult.Success -> {
                    pendingChanges = AppSettings()
                    _uiState.value = SettingsUiState.Success(settings = AppSettings())
                    _effects.send(SettingsUiEffect.ShowSnackbar("Settings reset to defaults"))
                }
                is CoreResult.Failure -> {
                    _effects.send(SettingsUiEffect.ShowSnackbar(
                        result.error.message ?: "Failed to reset settings"
                    ))
                }
                null -> { /* noop */ }
            }
        }
    }
}
