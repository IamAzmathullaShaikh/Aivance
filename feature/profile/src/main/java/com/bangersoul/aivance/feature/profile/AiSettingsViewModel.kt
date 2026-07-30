package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.DisableProviderUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.EnableProviderUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.GetAvailableModelsUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.GetProviderHealthUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.SelectProviderUseCase
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

data class AiProviderConfig(
    val providerId: String = "gemini",
    val providerName: String = "Google Gemini",
    val apiKey: String = "",
    val selectedModel: String = "",
    val availableModels: List<String> = emptyList(),
    val temperature: Float = 0.7f,
    val isConnected: Boolean = false,
    val isTesting: Boolean = false
)

enum class ConnectionStatus {
    IDLE,
    TESTING,
    CONNECTED,
    FAILED
}

sealed interface AiSettingsUiState {
    data object Loading : AiSettingsUiState
    data class Success(
        val config: AiProviderConfig = AiProviderConfig(),
        val connectionStatus: ConnectionStatus = ConnectionStatus.IDLE,
        val availableProviders: List<ProviderOption> = listOf(
            ProviderOption("gemini", "Google Gemini"),
            ProviderOption("openai", "OpenAI"),
            ProviderOption("groq", "Groq"),
            ProviderOption("openrouter", "OpenRouter"),
            ProviderOption("ollama", "Ollama (Local)")
        )
    ) : AiSettingsUiState
    data class Error(val message: String) : AiSettingsUiState
}

data class ProviderOption(val id: String, val name: String)

sealed interface AiSettingsUiEvent {
    data class SelectProvider(val providerId: String) : AiSettingsUiEvent
    data class SetApiKey(val apiKey: String) : AiSettingsUiEvent
    data class SelectModel(val model: String) : AiSettingsUiEvent
    data class SetTemperature(val temperature: Float) : AiSettingsUiEvent
    data object TestConnection : AiSettingsUiEvent
    data object SaveConfig : AiSettingsUiEvent
}

sealed interface AiSettingsUiEffect {
    data class ShowSnackbar(val message: String) : AiSettingsUiEffect
    data class ConnectionResult(val success: Boolean, val message: String) : AiSettingsUiEffect
}

@HiltViewModel
class AiSettingsViewModel @Inject constructor(
    private val enableProviderUseCase: EnableProviderUseCase,
    private val disableProviderUseCase: DisableProviderUseCase,
    private val selectProviderUseCase: SelectProviderUseCase,
    private val getProviderHealthUseCase: GetProviderHealthUseCase,
    private val getAvailableModelsUseCase: GetAvailableModelsUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiSettingsUiState>(AiSettingsUiState.Loading)
    val uiState: StateFlow<AiSettingsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<AiSettingsUiEffect>(Channel.BUFFERED)
    val effects: Flow<AiSettingsUiEffect> = _effects.receiveAsFlow()

    init {
        loadConfig()
    }

    fun onEvent(event: AiSettingsUiEvent) {
        when (event) {
            is AiSettingsUiEvent.SelectProvider -> selectProvider(event.providerId)
            is AiSettingsUiEvent.SetApiKey -> setApiKey(event.apiKey)
            is AiSettingsUiEvent.SelectModel -> selectModel(event.model)
            is AiSettingsUiEvent.SetTemperature -> setTemperature(event.temperature)
            AiSettingsUiEvent.TestConnection -> testConnection()
            AiSettingsUiEvent.SaveConfig -> saveConfig()
        }
    }

    private fun loadConfig() {
        viewModelScope.launch {
            _uiState.value = AiSettingsUiState.Loading

            val modelsResult = getAvailableModelsUseCase().firstOrNull()
            val models = when (modelsResult) {
                is CoreResult.Success -> modelsResult.data
                else -> emptyList()
            }

            _uiState.value = AiSettingsUiState.Success(
                config = AiProviderConfig(
                    availableModels = models,
                    selectedModel = models.firstOrNull() ?: "gemini-2.0-flash"
                )
            )
        }
    }

    private fun selectProvider(providerId: String) {
        val currentState = _uiState.value as? AiSettingsUiState.Success ?: return
        _uiState.value = currentState.copy(
            config = currentState.config.copy(providerId = providerId),
            connectionStatus = ConnectionStatus.IDLE
        )
    }

    private fun setApiKey(apiKey: String) {
        val currentState = _uiState.value as? AiSettingsUiState.Success ?: return
        _uiState.value = currentState.copy(
            config = currentState.config.copy(apiKey = apiKey)
        )
    }

    private fun selectModel(model: String) {
        val currentState = _uiState.value as? AiSettingsUiState.Success ?: return
        _uiState.value = currentState.copy(
            config = currentState.config.copy(selectedModel = model)
        )
    }

    private fun setTemperature(temperature: Float) {
        val currentState = _uiState.value as? AiSettingsUiState.Success ?: return
        _uiState.value = currentState.copy(
            config = currentState.config.copy(temperature = temperature)
        )
    }

    private fun testConnection() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "ai_settings_test_connection"))
            val state = _uiState.value as? AiSettingsUiState.Success ?: return@launch
            _uiState.value = state.copy(connectionStatus = ConnectionStatus.TESTING)

            val result = getProviderHealthUseCase(state.config.providerId).firstOrNull()
            when (result) {
                is CoreResult.Success -> {
                    val isHealthy = result.data
                    _uiState.value = state.copy(
                        config = state.config.copy(isConnected = isHealthy),
                        connectionStatus = if (isHealthy) ConnectionStatus.CONNECTED else ConnectionStatus.FAILED
                    )
                    _effects.send(AiSettingsUiEffect.ConnectionResult(
                        success = isHealthy,
                        message = if (isHealthy) "Connected successfully" else "Connection failed"
                    ))
                }
                is CoreResult.Failure -> {
                    _uiState.value = state.copy(connectionStatus = ConnectionStatus.FAILED)
                    _effects.send(AiSettingsUiEffect.ConnectionResult(
                        success = false,
                        message = result.error.message ?: "Connection failed"
                    ))
                }
                null -> { /* noop */ }
            }
        }
    }

    private fun saveConfig() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "ai_settings_save"))
            val state = _uiState.value as? AiSettingsUiState.Success ?: return@launch

            val result = selectProviderUseCase(state.config.providerId).firstOrNull()
            when (result) {
                is CoreResult.Success -> {
                    _effects.send(AiSettingsUiEffect.ShowSnackbar("Configuration saved"))
                }
                is CoreResult.Failure -> {
                    _effects.send(AiSettingsUiEffect.ShowSnackbar(
                        result.error.message ?: "Failed to save configuration"
                    ))
                }
                null -> { /* noop */ }
            }
        }
    }
}
