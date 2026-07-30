package com.bangersoul.aivance.core.domain.usecase.provider

import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Disables an AI provider, preventing it from being used.
 *
 * Business rules:
 * - Provider must exist in the configuration.
 * - Disabling preserves the provider configuration (API key, model, etc.).
 * - A disabled provider will not be selected by the ProviderManager.
 */
class DisableProviderUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<String, CoreResult<Unit>>() {

    override suspend operator fun invoke(providerId: String): CoreResult<Unit> {
        if (providerId.isBlank()) {
            return Result.Failure(ValidationError("providerId", "Provider ID cannot be blank."))
        }

        return runCatchingCore {
            val configsResult = settingsRepository.getAiProviderConfigs().firstOrNull()
            val configs = when (configsResult) {
                is Result.Success -> configsResult.data
                is Result.Failure -> throw Exception(configsResult.error.message)
                null -> throw Exception("Failed to load provider configs.")
            }

            val config = configs.find { it.providerId == providerId }
                ?: throw Exception("Provider not found: $providerId")

            val disabledConfig = config.copy(isEnabled = false)
            val result = settingsRepository.updateAiProviderConfig(disabledConfig)
            when (result) {
                is Result.Success -> Unit
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }
}
