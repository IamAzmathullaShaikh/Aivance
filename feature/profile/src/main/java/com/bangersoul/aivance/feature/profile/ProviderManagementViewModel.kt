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

data class ProviderInfo(
    val id: String,
    val name: String,
    val isEnabled: Boolean = false,
    val isConnected: Boolean = false,
    val selectedModel: String = "",
    val availableModels: List<String> = emptyList(),
    val apiKeyConfigured: Boolean = false,
    val healthStatus: ProviderHealthStatus = ProviderHealthStatus.UNKNOWN
)

enum class ProviderHealthStatus {
    HEALTHY,
    DEGRADED,
    UNHEALTHY,
    UNKNOWN
}

sealed interface ProviderManagementUiState {
    data object Loading : ProviderManagementUiState
    data class Success(
        val providers: List<ProviderInfo> = listOf(
            ProviderInfo("gemini", "Google Gemini"),
            ProviderInfo("openai", "OpenAI"),
            ProviderInfo("groq", "Groq"),
            ProviderInfo("openrouter", "OpenRouter"),
            ProviderInfo("ollama", "Ollama (Local)")
        ),
        val selectedProviderId: String = "gemini",
        val isTestingConnection: Boolean = false
    ) : ProviderManagementUiState
    data class Error(val message: String) : ProviderManagementUiState
}

sealed interface ProviderManagementUiEvent {
    data class SelectProvider(val providerId: String) : ProviderManagementUiEvent
    data class ToggleProvider(val providerId: String, val enabled: Boolean) : ProviderManagementUiEvent
    data class TestConnection(val providerId: String) : ProviderManagementUiEvent
    data class SetApiKey(val providerId: String, val apiKey: String) : ProviderManagementUiEvent
    data class SelectModel(val providerId: String, val model: String) : ProviderManagementUiEvent
    data object LoadModels : ProviderManagementUiEvent
    data object Refresh : ProviderManagementUiEvent
}

sealed interface ProviderManagementUiEffect {
    data class ShowSnackbar(val message: String) : ProviderManagementUiEffect
    data class ConnectionTestResult(val providerId: String, val success: Boolean, val message: String) : ProviderManagementUiEffect
    data object ProviderChanged : ProviderManagementUiEffect
}

@HiltViewModel
class ProviderManagementViewModel @Inject constructor(
    private val enableProviderUseCase: EnableProviderUseCase,
    private val disableProviderUseCase: DisableProviderUseCase,
    private val selectProviderUseCase: SelectProviderUseCase,
    private val getProviderHealthUseCase: GetProviderHealthUseCase,
    private val getAvailableModelsUseCase: GetAvailableModelsUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProviderManagementUiState>(ProviderManagementUiState.Loading)
    val uiState: StateFlow<ProviderManagementUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ProviderManagementUiEffect>(Channel.BUFFERED)
    val effects: Flow<ProviderManagementUiEffect> = _effects.receiveAsFlow()

    init {
        loadProviders()
    }

    fun onEvent(event: ProviderManagementUiEvent) {
        when (event) {
            is ProviderManagementUiEvent.SelectProvider -> selectProvider(event.providerId)
            is ProviderManagementUiEvent.ToggleProvider -> toggleProvider(event.providerId, event.enabled)
            is ProviderManagementUiEvent.TestConnection -> testConnection(event.providerId)
            is ProviderManagementUiEvent.SetApiKey -> setApiKey(event.providerId, event.apiKey)
            is ProviderManagementUiEvent.SelectModel -> selectModel(event.providerId, event.model)
            ProviderManagementUiEvent.LoadModels -> loadModels()
            ProviderManagementUiEvent.Refresh -> loadProviders()
        }
    }

    private fun loadProviders() {
        viewModelScope.launch {
            _uiState.value = ProviderManagementUiState.Loading
            trackEventUseCase(TrackEventRequest(eventName = "provider_mgmt_load"))

            // Load available models
            val modelsResult = getAvailableModelsUseCase().firstOrNull()
            val models = when (modelsResult) {
                is CoreResult.Success -> modelsResult.data
                else -> emptyList()
            }

            _uiState.value = ProviderManagementUiState.Success(
                providers = listOf(
                    ProviderInfo("gemini", "Google Gemini", isEnabled = true, availableModels = models, selectedModel = models.firstOrNull() ?: "gemini-2.0-flash"),
                    ProviderInfo("openai", "OpenAI", availableModels = models),
                    ProviderInfo("groq", "Groq"),
                    ProviderInfo("openrouter", "OpenRouter"),
                    ProviderInfo("ollama", "Ollama (Local)")
                ),
                selectedProviderId = "gemini"
            )
        }
    }

    private fun selectProvider(providerId: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "provider_select_$providerId"))
            val result = selectProviderUseCase(providerId).firstOrNull()
            when (result) {
                is CoreResult.Success -> {
                    val currentState = _uiState.value
                    if (currentState is ProviderManagementUiState.Success) {
                        _uiState.value = currentState.copy(selectedProviderId = providerId)
                    }
                    _effects.send(ProviderManagementUiEffect.ProviderChanged)
                    _effects.send(ProviderManagementUiEffect.ShowSnackbar("Provider changed to $providerId"))
                }
                is CoreResult.Failure -> {
                    _effects.send(ProviderManagementUiEffect.ShowSnackbar(
                        result.error.message ?: "Failed to select provider"
                    ))
                }
                null -> { /* noop */ }
            }
        }
    }

    private fun toggleProvider(providerId: String, enabled: Boolean) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "provider_toggle_${providerId}_$enabled"))
            val result = if (enabled) {
                enableProviderUseCase(providerId).firstOrNull()
            } else {
                disableProviderUseCase(providerId).firstOrNull()
            }
            when (result) {
                is CoreResult.Success -> {
                    val currentState = _uiState.value
                    if (currentState is ProviderManagementUiState.Success) {
                        val updatedProviders = currentState.providers.map {
                            if (it.id == providerId) it.copy(isEnabled = enabled) else it
                        }
                        _uiState.value = currentState.copy(providers = updatedProviders)
                    }
                    _effects.send(ProviderManagementUiEffect.ShowSnackbar(
                        if (enabled) "Provider enabled" else "Provider disabled"
                    ))
                }
                is CoreResult.Failure -> {
                    _effects.send(ProviderManagementUiEffect.ShowSnackbar(
                        result.error.message ?: "Failed to toggle provider"
                    ))
                }
                null -> { /* noop */ }
            }
        }
    }

    private fun testConnection(providerId: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "provider_test_connection_$providerId"))
            val currentState = _uiState.value
            if (currentState is ProviderManagementUiState.Success) {
                _uiState.value = currentState.copy(isTestingConnection = true)

                val result = getProviderHealthUseCase(providerId).firstOrNull()
                when (result) {
                    is CoreResult.Success -> {
                        val isHealthy = result.data
                        val updatedProviders = currentState.providers.map {
                            if (it.id == providerId) it.copy(
                                isConnected = isHealthy,
                                healthStatus = if (isHealthy) ProviderHealthStatus.HEALTHY
                                else ProviderHealthStatus.UNHEALTHY
                            ) else it
                        }
                        _uiState.value = currentState.copy(providers = updatedProviders, isTestingConnection = false)
                        _effects.send(ProviderManagementUiEffect.ConnectionTestResult(
                            providerId = providerId,
                            success = isHealthy,
                            message = if (isHealthy) "Connected successfully" else "Connection failed"
                        ))
                    }
                    is CoreResult.Failure -> {
                        _uiState.value = currentState.copy(isTestingConnection = false)
                        _effects.send(ProviderManagementUiEffect.ConnectionTestResult(
                            providerId = providerId,
                            success = false,
                            message = result.error.message ?: "Connection failed"
                        ))
                    }
                    null -> {
                        _uiState.value = currentState.copy(isTestingConnection = false)
                    }
                }
            }
        }
    }

    private fun setApiKey(providerId: String, apiKey: String) {
        val currentState = _uiState.value
        if (currentState is ProviderManagementUiState.Success) {
            val updatedProviders = currentState.providers.map {
                if (it.id == providerId) it.copy(apiKeyConfigured = apiKey.isNotBlank()) else it
            }
            _uiState.value = currentState.copy(providers = updatedProviders)
        }
    }

    private fun selectModel(providerId: String, model: String) {
        val currentState = _uiState.value
        if (currentState is ProviderManagementUiState.Success) {
            val updatedProviders = currentState.providers.map {
                if (it.id == providerId) it.copy(selectedModel = model) else it
            }
            _uiState.value = currentState.copy(providers = updatedProviders)
        }
    }

    private fun loadModels() {
        viewModelScope.launch {
            val result = getAvailableModelsUseCase().firstOrNull()
            when (result) {
                is CoreResult.Success -> {
                    val currentState = _uiState.value
                    if (currentState is ProviderManagementUiState.Success) {
                        val updatedProviders = currentState.providers.map {
                            if (it.selectedModel.isBlank()) it.copy(
                                availableModels = result.data,
                                selectedModel = result.data.firstOrNull() ?: ""
                            ) else it
                        }
                        _uiState.value = currentState.copy(providers = updatedProviders)
                    }
                }
                else -> { /* silent fail */ }
            }
        }
    }
}
