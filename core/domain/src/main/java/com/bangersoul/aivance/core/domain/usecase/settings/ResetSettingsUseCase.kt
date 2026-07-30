package com.bangersoul.aivance.core.domain.usecase.settings

import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import com.bangersoul.aivance.core.domain.usecase.NoInputUseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Resets all settings to their default values.
 *
 * Business rules:
 * - Removes all AI provider configurations.
 * - Does not delete user data (resumes, applications, etc.).
 * - Requires confirmation before execution (handled at UI layer).
 */
class ResetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : NoInputUseCase<CoreResult<Unit>>() {

    override suspend operator fun invoke(): CoreResult<Unit> {
        return runCatchingCore {
            val configsResult = settingsRepository.getAiProviderConfigs().firstOrNull()
            val configs = when (configsResult) {
                is Result.Success -> configsResult.data
                is Result.Failure -> emptyList()
                null -> emptyList()
            }

            // Disable all providers
            configs.forEach { config ->
                val disabledConfig = config.copy(isEnabled = false)
                settingsRepository.updateAiProviderConfig(disabledConfig)
            }
        }
    }
}
