package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ProviderRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.ProviderMetadata
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

sealed interface OnboardingUiState {
    data object Welcome : OnboardingUiState

    data class ChooseAiProvider(
        val providers: List<ProviderMetadata>
    ) : OnboardingUiState

    data class ConfigureAiProvider(
        val provider: ProviderMetadata,
        val config: Map<String, String> = emptyMap(),
        val isValidating: Boolean = false,
        val error: String? = null
    ) : OnboardingUiState

    data class ChooseJobProvider(
        val providers: List<ProviderMetadata>
    ) : OnboardingUiState

    data class ConfigureJobProvider(
        val provider: ProviderMetadata,
        val config: Map<String, String> = emptyMap(),
        val isValidating: Boolean = false,
        val error: String? = null
    ) : OnboardingUiState

    data class ChooseEnrichmentProvider(
        val providers: List<ProviderMetadata>
    ) : OnboardingUiState

    data class ConfigureEnrichmentProvider(
        val provider: ProviderMetadata,
        val config: Map<String, String> = emptyMap(),
        val isValidating: Boolean = false,
        val error: String? = null
    ) : OnboardingUiState

    data class Summary(
        val aiProvider: String,
        val jobProvider: String,
        val enrichmentProvider: String? = null
    ) : OnboardingUiState

    data object Complete : OnboardingUiState
}

sealed interface OnboardingUiEvent {
    data object Start : OnboardingUiEvent
    data class SelectAiProvider(val providerId: String) : OnboardingUiEvent
    data class UpdateAiConfig(val key: String, val value: String) : OnboardingUiEvent
    data object ValidateAiProvider : OnboardingUiEvent

    data class SelectJobProvider(val providerId: String) : OnboardingUiEvent
    data class UpdateJobConfig(val key: String, val value: String) : OnboardingUiEvent
    data object ValidateJobProvider : OnboardingUiEvent

    data class SelectEnrichmentProvider(val providerId: String) : OnboardingUiEvent
    data class UpdateEnrichmentConfig(val key: String, val value: String) : OnboardingUiEvent
    data object ValidateEnrichmentProvider : OnboardingUiEvent
    data object SkipEnrichment : OnboardingUiEvent

    data object Finish : OnboardingUiEvent
    data object Back : OnboardingUiEvent
}

sealed interface OnboardingUiEffect {
    data class ShowSnackbar(val message: String) : OnboardingUiEffect
    data object NavigateToHome : OnboardingUiEffect
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val providerRegistry: ProviderRegistry,
    private val providerManager: ProviderManager,
    private val providerRepository: ProviderRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingUiState>(OnboardingUiState.Welcome)
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _effects = Channel<OnboardingUiEffect>(Channel.BUFFERED)
    val effects: Flow<OnboardingUiEffect> = _effects.receiveAsFlow()

    private var selectedAiProviderId: String? = null
    private var selectedJobProviderId: String? = null
    private var selectedEnrichmentProviderId: String? = null

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            OnboardingUiEvent.Start -> {
                _uiState.value = OnboardingUiState.ChooseAiProvider(
                    providers = providerRegistry.getAllProviders()
                        .filter { it.metadata.type == ProviderType.AI }
                        .map { it.metadata }
                )
            }
            is OnboardingUiEvent.SelectAiProvider -> {
                selectedAiProviderId = event.providerId
                providerRegistry.getProvider(event.providerId)?.let {
                    _uiState.value = OnboardingUiState.ConfigureAiProvider(provider = it.metadata)
                }
            }
            is OnboardingUiEvent.UpdateAiConfig -> {
                val current = _uiState.value as? OnboardingUiState.ConfigureAiProvider ?: return
                _uiState.value = current.copy(config = current.config + (event.key to event.value))
            }
            OnboardingUiEvent.ValidateAiProvider -> validateAiProvider()

            is OnboardingUiEvent.SelectJobProvider -> {
                selectedJobProviderId = event.providerId
                providerRegistry.getProvider(event.providerId)?.let {
                    _uiState.value = OnboardingUiState.ConfigureJobProvider(provider = it.metadata)
                }
            }
            is OnboardingUiEvent.UpdateJobConfig -> {
                val current = _uiState.value as? OnboardingUiState.ConfigureJobProvider ?: return
                _uiState.value = current.copy(config = current.config + (event.key to event.value))
            }
            OnboardingUiEvent.ValidateJobProvider -> validateJobProvider()

            is OnboardingUiEvent.SelectEnrichmentProvider -> {
                selectedEnrichmentProviderId = event.providerId
                providerRegistry.getProvider(event.providerId)?.let {
                    _uiState.value = OnboardingUiState.ConfigureEnrichmentProvider(provider = it.metadata)
                }
            }
            is OnboardingUiEvent.UpdateEnrichmentConfig -> {
                val current = _uiState.value as? OnboardingUiState.ConfigureEnrichmentProvider ?: return
                _uiState.value = current.copy(config = current.config + (event.key to event.value))
            }
            OnboardingUiEvent.ValidateEnrichmentProvider -> validateEnrichmentProvider()
            OnboardingUiEvent.SkipEnrichment -> {
                selectedEnrichmentProviderId = null
                _uiState.value = OnboardingUiState.Summary(
                    aiProvider = providerRegistry.getProvider(selectedAiProviderId!!)?.metadata?.name ?: "Unknown",
                    jobProvider = providerRegistry.getProvider(selectedJobProviderId!!)?.metadata?.name ?: "Unknown",
                    enrichmentProvider = null
                )
            }

            OnboardingUiEvent.Finish -> {
                viewModelScope.launch {
                    trackEventUseCase(TrackEventRequest(eventName = "onboarding_finish"))
                    _effects.send(OnboardingUiEffect.NavigateToHome)
                }
            }
            OnboardingUiEvent.Back -> handleBack()
        }
    }

    private fun validateAiProvider() {
        val current = _uiState.value as? OnboardingUiState.ConfigureAiProvider ?: return
        val providerId = selectedAiProviderId ?: return

        viewModelScope.launch {
            _uiState.value = current.copy(isValidating = true, error = null)
            val config = ProviderConfiguration(
                providerId = providerId,
                secrets = mapOf("apiKey" to (current.config["apiKey"] ?: "")),
                settings = current.config - "apiKey" + ("type" to "AI")
            )
            val result = providerManager.validateProvider(providerId, config)
            if (result is Result.Success) {
                providerRepository.saveProviderConfig(config)
                _uiState.value = OnboardingUiState.ChooseJobProvider(
                    providers = providerRegistry.getAllProviders()
                        .filter { it.metadata.type == ProviderType.JOB }
                        .map { it.metadata }
                )
            } else {
                _uiState.value = current.copy(isValidating = false, error = (result as? Result.Failure)?.error?.message ?: "Validation failed")
            }
        }
    }

    private fun validateJobProvider() {
        val current = _uiState.value as? OnboardingUiState.ConfigureJobProvider ?: return
        val providerId = selectedJobProviderId ?: return

        viewModelScope.launch {
            _uiState.value = current.copy(isValidating = true, error = null)
            val config = ProviderConfiguration(
                providerId = providerId,
                secrets = mapOf("apiKey" to (current.config["apiKey"] ?: "")),
                settings = current.config - "apiKey" + ("type" to "JOB")
            )
            val result = providerManager.validateProvider(providerId, config)
            if (result is Result.Success) {
                providerRepository.saveProviderConfig(config)
                _uiState.value = OnboardingUiState.ChooseEnrichmentProvider(
                    providers = providerRegistry.getAllProviders()
                        .filter { it.metadata.type == ProviderType.ENRICHMENT }
                        .map { it.metadata }
                )
            } else {
                _uiState.value = current.copy(isValidating = false, error = (result as? Result.Failure)?.error?.message ?: "Validation failed")
            }
        }
    }

    private fun validateEnrichmentProvider() {
        val current = _uiState.value as? OnboardingUiState.ConfigureEnrichmentProvider ?: return
        val providerId = selectedEnrichmentProviderId ?: return

        viewModelScope.launch {
            _uiState.value = current.copy(isValidating = true, error = null)
            val config = ProviderConfiguration(
                providerId = providerId,
                secrets = mapOf("apiKey" to (current.config["apiKey"] ?: "")),
                settings = current.config - "apiKey" + ("type" to "ENRICHMENT")
            )
            val result = providerManager.validateProvider(providerId, config)
            if (result is Result.Success) {
                providerRepository.saveProviderConfig(config)
                _uiState.value = OnboardingUiState.Summary(
                    aiProvider = providerRegistry.getProvider(selectedAiProviderId!!)?.metadata?.name ?: "Unknown",
                    jobProvider = providerRegistry.getProvider(selectedJobProviderId!!)?.metadata?.name ?: "Unknown",
                    enrichmentProvider = providerRegistry.getProvider(selectedEnrichmentProviderId!!)?.metadata?.name
                )
            } else {
                _uiState.value = current.copy(isValidating = false, error = (result as? Result.Failure)?.error?.message ?: "Validation failed")
            }
        }
    }

    private fun handleBack() {
        when (_uiState.value) {
            is OnboardingUiState.ChooseAiProvider -> _uiState.value = OnboardingUiState.Welcome
            is OnboardingUiState.ConfigureAiProvider -> {
                 _uiState.value = OnboardingUiState.ChooseAiProvider(
                    providers = providerRegistry.getAllProviders()
                        .filter { it.metadata.type == ProviderType.AI }
                        .map { it.metadata }
                )
            }
            is OnboardingUiState.ChooseJobProvider -> {
                providerRegistry.getProvider(selectedAiProviderId!!)?.let {
                    _uiState.value = OnboardingUiState.ConfigureAiProvider(provider = it.metadata)
                }
            }
            is OnboardingUiState.ConfigureJobProvider -> {
                _uiState.value = OnboardingUiState.ChooseJobProvider(
                    providers = providerRegistry.getAllProviders()
                        .filter { it.metadata.type == ProviderType.JOB }
                        .map { it.metadata }
                )
            }
            is OnboardingUiState.ChooseEnrichmentProvider -> {
                providerRegistry.getProvider(selectedJobProviderId!!)?.let {
                    _uiState.value = OnboardingUiState.ConfigureJobProvider(provider = it.metadata)
                }
            }
            is OnboardingUiState.ConfigureEnrichmentProvider -> {
                _uiState.value = OnboardingUiState.ChooseEnrichmentProvider(
                    providers = providerRegistry.getAllProviders()
                        .filter { it.metadata.type == ProviderType.ENRICHMENT }
                        .map { it.metadata }
                )
            }
            is OnboardingUiState.Summary -> {
                if (selectedEnrichmentProviderId != null) {
                    providerRegistry.getProvider(selectedEnrichmentProviderId!!)?.let {
                        _uiState.value = OnboardingUiState.ConfigureEnrichmentProvider(provider = it.metadata)
                    }
                } else {
                    _uiState.value = OnboardingUiState.ChooseEnrichmentProvider(
                        providers = providerRegistry.getAllProviders()
                            .filter { it.metadata.type == ProviderType.ENRICHMENT }
                            .map { it.metadata }
                    )
                }
            }
            else -> {}
        }
    }
}
