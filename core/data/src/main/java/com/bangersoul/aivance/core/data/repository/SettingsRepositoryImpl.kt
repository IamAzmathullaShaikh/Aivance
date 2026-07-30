package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.model.JobScraperConfig
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.onSuccess
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.cache.CacheManager
import com.bangersoul.aivance.core.data.source.AiLocalDataSource
import com.bangersoul.aivance.core.data.source.SettingsLocalDataSource
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsLocalDataSource: SettingsLocalDataSource,
    private val aiLocalDataSource: AiLocalDataSource,
    private val configCache: CacheManager<String, List<AiProviderConfig>>
) : SettingsRepository {

    private val AI_CONFIG_CACHE_KEY = "ai_provider_configs"

    override fun getAiProviderConfigs(): Flow<CoreResult<List<AiProviderConfig>>> {
        return aiLocalDataSource.getProviderConfigs().map { configs ->
            runCatchingCore { configs }.also { result ->
                result.onSuccess { configCache.put(AI_CONFIG_CACHE_KEY, it) }
            }
        }
    }

    override suspend fun updateAiProviderConfig(config: AiProviderConfig): CoreResult<Unit> = runCatchingCore {
        aiLocalDataSource.saveProviderConfig(config)
        configCache.evict(AI_CONFIG_CACHE_KEY)
    }

    override fun getScraperConfig(): Flow<CoreResult<JobScraperConfig>> {
        return settingsLocalDataSource.userPreferences.map {
            runCatchingCore {
                JobScraperConfig(
                    providerId = "apify",
                    apifyToken = it.geminiApiKey ?: "", // Reusing field for now
                    activeActorId = "default"
                )
            }
        }
    }

    override suspend fun updateScraperConfig(config: JobScraperConfig): CoreResult<Unit> = runCatchingCore {
        settingsLocalDataSource.updateGeminiApiKey(config.apifyToken)
    }
}
