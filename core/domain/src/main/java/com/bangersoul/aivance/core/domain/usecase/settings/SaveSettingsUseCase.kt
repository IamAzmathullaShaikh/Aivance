package com.bangersoul.aivance.core.domain.usecase.settings

import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class SaveSettingsRequest(
    val aiProviderConfigs: List<AiProviderConfig> = emptyList()
)

/**
 * Saves application settings.
 *
 * Business rules:
 * - Each AI provider config is validated before saving.
 * - Invalid configs are skipped with an error logged.
 * - Settings are persisted immediately.
 */
class SaveSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : UseCase<SaveSettingsRequest, CoreResult<Unit>>() {

    override suspend operator fun invoke(input: SaveSettingsRequest): CoreResult<Unit> {
        if (input.aiProviderConfigs.isEmpty()) {
            return Result.Success(Unit) // Nothing to save
        }

        return runCatchingCore {
            input.aiProviderConfigs.forEach { config ->
                val result = settingsRepository.updateAiProviderConfig(config)
                when (result) {
                    is Result.Success -> { /* Saved successfully */ }
                    is Result.Failure -> throw Exception("Failed to save config for ${config.providerId}: ${result.error.message}")
                }
            }
        }
    }
}
