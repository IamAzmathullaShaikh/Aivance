package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.domain.repository.ProviderRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.GetAvailableModelsUseCase
import com.bangersoul.aivance.core.domain.usecase.provider.GetProviderHealthUseCase
import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.ProviderStatus
import com.bangersoul.aivance.sdk.core.ProviderType
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import com.bangersoul.aivance.sdk.infrastructure.ProviderRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProviderManagementUiState {
    data object Loading : ProviderManagementUiState
    data class Success(
        val providers: List<ProviderInfo> = emptyList(),
        val selectedProviderId: String? = null,
        val isTestingConnection: Boolean = false,
        val testingProviderId: String? = null,
        val apiKeyDrafts: Map<String, String> = emptyMap()
    ) : ProviderManagementUiState
    data class Error(val message: String) : ProviderManagementUiState
}

sealed interface ProviderManagementUiEvent {
    data class SelectProvider(val providerId: String) : ProviderManagementUiEvent
    data class ToggleProvider(val providerId: String, val enabled: Boolean) : ProviderManagementUiEvent
    data class TestConnection(val providerId: String) : ProviderManagementUiEvent
    data class SetApiKey(val providerId: String, val apiKey: String) : ProviderManagementUiEvent
    data class SelectModel(val providerId: String, val model: String) : ProviderManagementUiEvent
    data class SaveProvider(val providerId: String) : ProviderManagementUiEvent
    data object Refresh : ProviderManagementUiEvent
}

sealed interface ProviderManagementUiEffect {
    data class ShowSnackbar(val message: String) : ProviderManagementUiEffect
    data class ConnectionTestResult(val providerId: String, val success: Boolean, val message: String) : ProviderManagementUiEffect
    data object ProviderChanged : ProviderManagementUiEffect
}

@HiltViewModel
class ProviderManagementViewModel @Inject constructor(
    private val providerRegistry: ProviderRegistry,
    private val providerManager: ProviderManager,
    private val providerRepository: ProviderRepository,
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
            is ProviderManagementUiEvent.SaveProvider -> saveProvider(event.providerId)
            ProviderManagementUiEvent.Refresh -> loadProviders()
        }
    }

    /** Builds the full provider list from the SDK registry + persisted configs. */
    private fun loadProviders() {
        viewModelScope.launch {
            _uiState.value = ProviderManagementUiState.Loading
            trackEventUseCase(TrackEventRequest("provider_mgmt_load"))

            val statuses = providerManager.providerStatuses.value
            val providers = providerRegistry.getAllProviders().map { base ->
                val meta = base.metadata
                val persisted = providerRepository.getProviderConfig(meta.id)
                val models = meta.supportedModels.ifEmpty {
                    getAvailableModelsUseCase(meta.id).getOrNull() ?: emptyList()
                }
                val selectedModel = persisted?.settings?.get("selectedModel")
                    ?: persisted?.settings?.get("model")
                    ?: models.firstOrNull()
                    ?: ""

                val secretValue = persisted?.secrets?.values?.firstOrNull { it.isNotBlank() }
                ProviderInfo(
                    id = meta.id,
                    name = meta.name,
                    category = when (meta.type) {
                        ProviderType.AI -> ProviderCategory.AI
                        ProviderType.JOB -> ProviderCategory.JOB
                        ProviderType.ENRICHMENT -> ProviderCategory.ENRICHMENT
                    },
                    description = meta.description,
                    isEnabled = persisted?.settings?.get("isEnabled")?.toBoolean()
                        ?: (statuses[meta.id] == ProviderStatus.Active || statuses[meta.id] == ProviderStatus.Ready),
                    isConnected = secretValue != null,
                    selectedModel = selectedModel,
                    availableModels = models,
                    apiKeyConfigured = secretValue != null,
                    maskedApiKey = secretValue?.let { maskKey(it) }.orEmpty(),
                    healthStatus = mapStatus(statuses[meta.id] ?: base.status)
                )
            }.sortedBy { it.category.ordinal }

            _uiState.value = ProviderManagementUiState.Success(
                providers = providers,
                selectedProviderId = providers.firstOrNull { it.isEnabled }?.id
            )
        }
    }

    private fun selectProvider(providerId: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("provider_select_$providerId"))
            val currentState = _uiState.value as? ProviderManagementUiState.Success ?: return@launch

            // Persist selection + enable it so ProviderManager picks it up.
            val provider = currentState.providers.find { it.id == providerId }
            val config = buildConfig(provider, enabled = true)
            providerRepository.saveProviderConfig(config)

            _uiState.value = currentState.copy(selectedProviderId = providerId)
            _effects.send(ProviderManagementUiEffect.ProviderChanged)
            _effects.send(ProviderManagementUiEffect.ShowSnackbar("Provider changed to ${provider?.name ?: providerId}"))
        }
    }

    private fun toggleProvider(providerId: String, enabled: Boolean) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("provider_toggle_${providerId}_$enabled"))
            val currentState = _uiState.value as? ProviderManagementUiState.Success ?: return@launch
            val provider = currentState.providers.find { it.id == providerId } ?: return@launch

            val config = buildConfig(provider, enabled = enabled)
            val result = providerRepository.saveProviderConfig(config)

            val updatedProviders = currentState.providers.map {
                if (it.id == providerId) it.copy(isEnabled = enabled) else it
            }
            _uiState.value = currentState.copy(providers = updatedProviders)

            _effects.send(ProviderManagementUiEffect.ShowSnackbar(
                when {
                    result is Result.Success && enabled -> "Provider enabled"
                    result is Result.Success -> "Provider disabled"
                    result is Result.Failure -> result.error.message
                    else -> "Provider updated"
                }
            ))
        }
    }

    private fun setApiKey(providerId: String, apiKey: String) {
        val currentState = _uiState.value as? ProviderManagementUiState.Success ?: return
        _uiState.value = currentState.copy(
            apiKeyDrafts = currentState.apiKeyDrafts + (providerId to apiKey)
        )
    }

    private fun selectModel(providerId: String, model: String) {
        val currentState = _uiState.value as? ProviderManagementUiState.Success ?: return
        val updatedProviders = currentState.providers.map {
            if (it.id == providerId) it.copy(selectedModel = model) else it
        }
        _uiState.value = currentState.copy(providers = updatedProviders)
    }

    /** Persists the drafted API key + selected model for a provider. */
    private fun saveProvider(providerId: String) {
        viewModelScope.launch {
            val currentState = _uiState.value as? ProviderManagementUiState.Success ?: return@launch
            val provider = currentState.providers.find { it.id == providerId } ?: return@launch
            val draftKey = currentState.apiKeyDrafts[providerId].orEmpty()

            val config = buildConfig(
                provider = provider,
                apiKey = draftKey.ifBlank { null },
                enabled = provider.isEnabled
            )
            val result = providerRepository.saveProviderConfig(config)

            // Refresh persisted state so health/connection reflect the new key.
            refreshProvider(providerId)

            _effects.send(ProviderManagementUiEffect.ShowSnackbar(
                if (result is Result.Success) "${provider.name} saved" else (result as? Result.Failure)?.error?.message ?: "Failed to save ${provider.name}"
            ))
        }
    }

    /** Runs a real credential validation through the SDK's validateProvider. */
    private fun testConnection(providerId: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("provider_test_connection_$providerId"))
            val currentState = _uiState.value as? ProviderManagementUiState.Success ?: return@launch
            val provider = currentState.providers.find { it.id == providerId } ?: return@launch
            _uiState.value = currentState.copy(isTestingConnection = true, testingProviderId = providerId)

            val draftKey = currentState.apiKeyDrafts[providerId].orEmpty()
            val config = buildConfig(provider, apiKey = draftKey.ifBlank { null }, enabled = provider.isEnabled)

            // Persist the draft key first so validation runs against the real credential.
            providerRepository.saveProviderConfig(config)

            val result = providerManager.validateProvider(providerId, config)
            val status = providerManager.providerStatuses.value[providerId] ?: baseStatusFor(providerId)
            val isHealthy = result is Result.Success

            val updatedProviders = currentState.providers.map {
                if (it.id == providerId) it.copy(
                    isConnected = isHealthy,
                    apiKeyConfigured = isHealthy || it.apiKeyConfigured,
                    healthStatus = if (isHealthy) ProviderHealthStatus.HEALTHY else mapStatus(status)
                ) else it
            }
            _uiState.value = currentState.copy(
                providers = updatedProviders,
                isTestingConnection = false,
                testingProviderId = null
            )
            _effects.send(ProviderManagementUiEffect.ConnectionTestResult(
                providerId = providerId,
                success = isHealthy,
                message = if (isHealthy) "Connected successfully" else (result as? Result.Failure)?.error?.message ?: "Connection failed"
            ))
        }
    }

    private suspend fun refreshProvider(providerId: String) {
        val currentState = _uiState.value as? ProviderManagementUiState.Success ?: return
        val provider = currentState.providers.find { it.id == providerId } ?: return
        val persisted = providerRepository.getProviderConfig(providerId)
        val health = getProviderHealthUseCase(providerId)
        val healthStatus = when {
            health is Result.Success && health.data.isOperational -> ProviderHealthStatus.HEALTHY
            health is Result.Failure -> ProviderHealthStatus.UNHEALTHY
            else -> mapStatus(providerManager.providerStatuses.value[providerId] ?: baseStatusFor(providerId))
        }
        val updatedProviders = currentState.providers.map {
            if (it.id == providerId) it.copy(
                isEnabled = persisted?.settings?.get("isEnabled")?.toBoolean() ?: it.isEnabled,
                selectedModel = persisted?.settings?.get("selectedModel") ?: it.selectedModel,
                apiKeyConfigured = persisted?.secrets?.values?.any { s -> s.isNotBlank() } == true,
                isConnected = persisted?.secrets?.values?.any { s -> s.isNotBlank() } == true,
                healthStatus = healthStatus
            ) else it
        }
        _uiState.value = currentState.copy(providers = updatedProviders)
    }

    private fun buildConfig(
        provider: ProviderInfo?,
        apiKey: String? = null,
        enabled: Boolean
    ): ProviderConfiguration {
        val settings = mutableMapOf(
            "isEnabled" to enabled.toString(),
            "selectedModel" to (provider?.selectedModel ?: ""),
            "model" to (provider?.selectedModel ?: "")
        )
        val secrets = mutableMapOf<String, String>()
        if (!apiKey.isNullOrBlank()) {
            if (provider?.id == "adzuna") {
                if (apiKey.contains(":")) {
                    val parts = apiKey.split(":", limit = 2)
                    settings["appId"] = parts[0].trim()
                    secrets["appId"] = parts[0].trim()
                    secrets["appKey"] = parts[1].trim()
                } else {
                    secrets["appKey"] = apiKey.trim()
                }
            } else {
                secrets["apiKey"] = apiKey.trim()
            }
        }
        return ProviderConfiguration(
            providerId = provider?.id ?: "",
            settings = settings,
            secrets = secrets
        )
    }

    /**
     * Masks a credential for display: keeps the first 4 and last 4 characters
     * so users can confirm which key is configured without exposing it.
     */
    private fun maskKey(key: String): String {
        if (key.length <= 8) return "••••••••"
        return "${key.take(4)}••••${key.takeLast(4)}"
    }

    private fun baseStatusFor(providerId: String): ProviderStatus {
        return providerRegistry.getProvider(providerId)?.status ?: ProviderStatus.Uninitialized
    }

    private fun mapStatus(status: ProviderStatus): ProviderHealthStatus = when (status) {
        ProviderStatus.Active, ProviderStatus.Ready, ProviderStatus.Healthy -> ProviderHealthStatus.HEALTHY
        ProviderStatus.Degraded -> ProviderHealthStatus.DEGRADED
        ProviderStatus.Uninitialized, ProviderStatus.Initializing -> ProviderHealthStatus.UNKNOWN
        else -> ProviderHealthStatus.UNHEALTHY
    }
}
