package com.bangersoul.aivance.core.domain.usecase.provider

import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.common.validation.ConfigurationValidator
import com.bangersoul.aivance.core.common.validation.ValidationResult
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Enables an AI provider for use.
 *
 * Business rules:
 * - Provider must be configured with a valid API key (except for local providers like Ollama).
 * - Provider configuration is persisted.
 * - Enabling a provider makes it available for selection by the ProviderManager.
 */
class EnableProviderUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<AiProviderConfig, CoreResult<Unit>>() {

    override suspend operator fun invoke(config: AiProviderConfig): CoreResult<Unit> {
        if (config.providerId.isBlank()) {
            return Result.Failure(ValidationError("providerId", "Provider ID cannot be blank."))
        }

        val validation = ConfigurationValidator.validate(config.copy(isEnabled = true))
        if (validation is ValidationResult.Invalid) {
            return Result.Failure(ValidationError(
                field = validation.errors.first().field,
                message = validation.errors.first().message
            ))
        }

        return runCatchingCore {
            val enabledConfig = config.copy(isEnabled = true)
            val result = settingsRepository.updateAiProviderConfig(enabledConfig)
            when (result) {
                is Result.Success -> Unit
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }
}
