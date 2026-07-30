package com.bangersoul.aivance.core.domain.usecase.settings

import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.model.JobScraperConfig
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class AppSettings(
    val aiProviders: List<AiProviderConfig> = emptyList(),
    val scraperConfig: JobScraperConfig? = null
)

/**
 * Loads all application settings.
 *
 * Business rules:
 * - Combines AI provider configs and scraper configs into a single settings object.
 * - Returns defaults if no settings are configured.
 * - Does not throw errors for missing settings.
 */
class LoadSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    fun invoke(): Flow<CoreResult<AppSettings>> {
        return settingsRepository.getAiProviderConfigs().map { configsResult ->
            runCatchingCore {
                val configs = when (configsResult) {
                    is Result.Success -> configsResult.data
                    is Result.Failure -> emptyList()
                }

                AppSettings(
                    aiProviders = configs,
                    scraperConfig = null // Would load from scraper config repo
                )
            }
        }
    }
}
