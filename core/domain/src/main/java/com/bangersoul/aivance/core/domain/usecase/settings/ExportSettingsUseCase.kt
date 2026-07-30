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
 * Exports all settings as a JSON string.
 *
 * Business rules:
 * - Exports AI provider configurations (excluding API keys for security).
 * - Returns a JSON representation of the settings.
 * - API keys are masked in the export for security.
 */
class ExportSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) : NoInputUseCase<CoreResult<String>>() {

    override suspend operator fun invoke(): CoreResult<String> {
        return runCatchingCore {
            val configsResult = settingsRepository.getAiProviderConfigs()
                .firstOrNull()

            val configs = when (configsResult) {
                is Result.Success -> configsResult.data
                is Result.Failure -> emptyList()
                null -> emptyList()
            }

            // Mask API keys for security
            val sanitizedConfigs = configs.map { config ->
                config.copy(
                    apiKey = if (config.apiKey.isNotBlank()) "••••${config.apiKey.takeLast(4)}" else ""
                )
            }

            buildString {
                appendLine("{")
                appendLine("  \"exportVersion\": \"1.0\",")
                appendLine("  \"exportDate\": \"${java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date(System.currentTimeMillis()))}\",")
                appendLine("  \"aiProviders\": [")
                sanitizedConfigs.forEachIndexed { index, config ->
                    appendLine("    {")
                    appendLine("      \"providerId\": \"${config.providerId}\",")
                    appendLine("      \"selectedModel\": \"${config.selectedModel}\",")
                    appendLine("      \"temperature\": ${config.temperature},")
                    appendLine("      \"maxTokens\": ${config.maxTokens},")
                    appendLine("      \"apiKey\": \"${config.apiKey}\",")
                    appendLine("      \"isEnabled\": ${config.isEnabled}")
                    append("    }")
                    if (index < sanitizedConfigs.size - 1) appendLine(",") else appendLine()
                }
                appendLine("  ]")
                append("}")
            }
        }
    }
}
