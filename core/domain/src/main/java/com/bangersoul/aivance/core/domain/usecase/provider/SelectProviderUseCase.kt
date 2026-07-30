package com.bangersoul.aivance.core.domain.usecase.provider

import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.common.validation.ProviderValidator
import com.bangersoul.aivance.core.common.validation.ValidationResult
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class SelectProviderRequest(
    val providerId: String,
    val apiKey: String = "",
    val selectedModel: String = "gemini-1.5-flash",
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val customBaseUrl: String? = null
)

/**
 * Selects and configures a specific AI provider for use.
 *
 * Business rules:
 * - Provider ID must be a known/supported provider.
 * - API key is required for cloud providers (Gemini, OpenAI, Groq, OpenRouter).
 * - Local providers (Ollama) do not require an API key.
 * - Saves the provider configuration and enables it.
 */
class SelectProviderUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<SelectProviderRequest, CoreResult<AiProviderConfig>>() {

    override suspend operator fun invoke(input: SelectProviderRequest): CoreResult<AiProviderConfig> {
        if (input.providerId.isBlank()) {
            return Result.Failure(ValidationError("providerId", "Provider ID cannot be blank."))
        }

        val providerValidation = ProviderValidator.validate(input.providerId)
        if (providerValidation is ValidationResult.Invalid) {
            return Result.Failure(ValidationError("providerId", providerValidation.errors.first().message))
        }

        // Ollama is local and doesn't require an API key
        val isLocalProvider = input.providerId == "OLLAMA"
        if (!isLocalProvider && input.apiKey.isBlank()) {
            return Result.Failure(ValidationError("apiKey", "API key is required for ${input.providerId}."))
        }

        return runCatchingCore {
            val config = AiProviderConfig(
                providerId = input.providerId,
                apiKey = input.apiKey,
                selectedModel = input.selectedModel,
                temperature = input.temperature.coerceIn(0.0f, 1.0f),
                maxTokens = input.maxTokens.coerceAtLeast(1),
                customBaseUrl = input.customBaseUrl,
                isEnabled = true
            )

            val result = settingsRepository.updateAiProviderConfig(config)
            when (result) {
                is Result.Success -> config
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }
}
