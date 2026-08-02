package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import com.bangersoul.aivance.core.domain.repository.ProviderRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.FieldType
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderType
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import com.bangersoul.aivance.sdk.infrastructure.ProviderRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OnboardingUiState {
    // v2: Welcome is its own destination (WelcomeScreen); the provider flow
    // starts directly at AI provider selection.

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

    /** Skip provider configuration entirely — providers can be set up later in Settings. */
    data object SkipAll : OnboardingUiEvent
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


@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val providerRegistry: ProviderRegistry,
    private val providerManager: ProviderManager,
    private val providerRepository: ProviderRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<OnboardingUiState>(
        OnboardingUiState.ChooseAiProvider(providers = emptyList())
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private var selectedAiProviderId: String? = null
    private var selectedJobProviderId: String? = null
    private var selectedEnrichmentProviderId: String? = null

    init {
        // v2: begin directly at AI provider selection — the Welcome step now
        // lives in the standalone WelcomeScreen destination.
        _uiState.value = OnboardingUiState.ChooseAiProvider(
            providers = providerRegistry.getAllProviders()
                .filter { it.metadata.type == ProviderType.AI }
                .map { it.metadata }
        )
    }

    // Per-step draft configs retained across Back navigation so a typed (or
    // pasted) API key is never silently wiped when the user leaves and re-enters
    // a provider configuration step.
    private val aiConfigDraft = mutableMapOf<String, String>()
    private val jobConfigDraft = mutableMapOf<String, String>()
    private val enrichmentConfigDraft = mutableMapOf<String, String>()

    fun onEvent(event: OnboardingUiEvent) {
        when (event) {
            // Kept for backward compatibility with the legacy Onboarding destination.
            OnboardingUiEvent.Start -> {
                _uiState.value = OnboardingUiState.ChooseAiProvider(
                    providers = providerRegistry.getAllProviders()
                        .filter { it.metadata.type == ProviderType.AI }
                        .map { it.metadata }
                )
            }
            OnboardingUiEvent.SkipAll -> skipAll()
            is OnboardingUiEvent.SelectAiProvider -> {
                // Drafts are per-step, not per-provider: never leak the previous
                // provider's key into a newly selected one's config screen.
                if (selectedAiProviderId != event.providerId) aiConfigDraft.clear()
                selectedAiProviderId = event.providerId
                providerRegistry.getProvider(event.providerId)?.let {
                    _uiState.value = OnboardingUiState.ConfigureAiProvider(
                        provider = it.metadata,
                        config = aiConfigDraft.toMap()
                    )
                }
            }
            is OnboardingUiEvent.UpdateAiConfig -> {
                val current = _uiState.value as? OnboardingUiState.ConfigureAiProvider ?: return
                aiConfigDraft[event.key] = event.value
                _uiState.value = current.copy(config = current.config + (event.key to event.value))
            }
            OnboardingUiEvent.ValidateAiProvider -> validateAiProvider()

            is OnboardingUiEvent.SelectJobProvider -> {
                if (selectedJobProviderId != event.providerId) jobConfigDraft.clear()
                selectedJobProviderId = event.providerId
                providerRegistry.getProvider(event.providerId)?.let {
                    _uiState.value = OnboardingUiState.ConfigureJobProvider(
                        provider = it.metadata,
                        config = jobConfigDraft.toMap()
                    )
                }
            }
            is OnboardingUiEvent.UpdateJobConfig -> {
                val current = _uiState.value as? OnboardingUiState.ConfigureJobProvider ?: return
                jobConfigDraft[event.key] = event.value
                _uiState.value = current.copy(config = current.config + (event.key to event.value))
            }
            OnboardingUiEvent.ValidateJobProvider -> validateJobProvider()

            is OnboardingUiEvent.SelectEnrichmentProvider -> {
                if (selectedEnrichmentProviderId != event.providerId) enrichmentConfigDraft.clear()
                selectedEnrichmentProviderId = event.providerId
                providerRegistry.getProvider(event.providerId)?.let {
                    _uiState.value = OnboardingUiState.ConfigureEnrichmentProvider(
                        provider = it.metadata,
                        config = enrichmentConfigDraft.toMap()
                    )
                }
            }
            is OnboardingUiEvent.UpdateEnrichmentConfig -> {
                val current = _uiState.value as? OnboardingUiState.ConfigureEnrichmentProvider ?: return
                enrichmentConfigDraft[event.key] = event.value
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
                    try {
                        trackEventUseCase(TrackEventRequest(eventName = "onboarding_finish"))
                        // Persist completion so a restart doesn't re-show onboarding, and the
                        // auth guard treats the user as onboarded.
                        userPreferencesRepository.updateOnboardingCompleted(true)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Non-blocking: onboarding must complete even if tracking or the
                        // persistence write hiccups.
                        android.util.Log.w("Onboarding", "Completion tracking/persistence failed", e)
                    } finally {
                        // The terminal state transition must always run so the
                        // "Go to Dashboard" button can never dead-end.
                        _uiState.value = OnboardingUiState.Complete
                    }
                }
            }
            OnboardingUiEvent.Back -> handleBack()
        }
    }

    private fun skipAll() {
        viewModelScope.launch {
            try {
                trackEventUseCase(TrackEventRequest(eventName = "onboarding_skip_all"))
                userPreferencesRepository.updateOnboardingCompleted(true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Non-blocking: skipping must complete even if tracking hiccups.
                android.util.Log.w("Onboarding", "Skip-all tracking failed", e)
            } finally {
                _uiState.value = OnboardingUiState.Complete
            }
        }
    }

    private fun validateAiProvider() {
        val current = _uiState.value as? OnboardingUiState.ConfigureAiProvider ?: return
        val providerId = selectedAiProviderId ?: return

        viewModelScope.launch {
            _uiState.value = current.copy(isValidating = true, error = null)
            val config = buildProviderConfig(
                providerId = providerId,
                type = "AI",
                config = current.config,
                metadata = current.provider
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
            val config = buildProviderConfig(
                providerId = providerId,
                type = "JOB",
                config = current.config,
                metadata = current.provider
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
            val config = buildProviderConfig(
                providerId = providerId,
                type = "ENRICHMENT",
                config = current.config,
                metadata = current.provider
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

    /**
     * Builds a [ProviderConfiguration] from the entered form fields, splitting
     * credentials correctly:
     *  - PASSWORD/sensitive fields from the provider's own metadata go into
     *    [ProviderConfiguration.secrets] (encrypted at rest),
     *  - everything else (model names, base URLs, non-sensitive IDs) goes into
     *    [ProviderConfiguration.settings] (plaintext preferences).
     *
     * Previously only a hardcoded "apiKey" key was treated as a secret, which
     * meant multi-credential providers (e.g. Adzuna's `appKey`) had their
     * primary credential stored in plaintext settings and never applied to the
     * live provider instance.
     */
    private fun buildProviderConfig(
        providerId: String,
        type: String,
        config: Map<String, String>,
        metadata: ProviderMetadata
    ): ProviderConfiguration {
        val secretKeys = metadata.configFields
            .filter { it.fieldType == FieldType.PASSWORD || it.isSensitive }
            .map { it.key }
            .toSet()
        return ProviderConfiguration(
            providerId = providerId,
            secrets = config.filterKeys { it in secretKeys },
            settings = config.filterKeys { it !in secretKeys } + ("type" to type)
        )
    }

    private fun handleBack() {
        when (_uiState.value) {
            is OnboardingUiState.ChooseAiProvider -> {
                // v2: no Welcome step inside onboarding — backing out from the
                // first step exits the flow entirely.
                _uiState.value = OnboardingUiState.Complete
            }
            is OnboardingUiState.ConfigureAiProvider -> {
                 _uiState.value = OnboardingUiState.ChooseAiProvider(
                    providers = providerRegistry.getAllProviders()
                        .filter { it.metadata.type == ProviderType.AI }
                        .map { it.metadata }
                )
            }
            is OnboardingUiState.ChooseJobProvider -> {
                providerRegistry.getProvider(selectedAiProviderId!!)?.let {
                    _uiState.value = OnboardingUiState.ConfigureAiProvider(
                        provider = it.metadata,
                        config = aiConfigDraft.toMap()
                    )
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
                    _uiState.value = OnboardingUiState.ConfigureJobProvider(
                        provider = it.metadata,
                        config = jobConfigDraft.toMap()
                    )
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
                        _uiState.value = OnboardingUiState.ConfigureEnrichmentProvider(
                            provider = it.metadata,
                            config = enrichmentConfigDraft.toMap()
                        )
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
