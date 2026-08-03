package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.model.JobScraperConfig
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.onSuccess
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.cache.CacheManager
import com.bangersoul.aivance.core.data.source.AiLocalDataSource
import com.bangersoul.aivance.core.data.source.SettingsLocalDataSource
import com.bangersoul.aivance.core.database.security.EncryptionService
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val settingsLocalDataSource: SettingsLocalDataSource,
    private val aiLocalDataSource: AiLocalDataSource,
    private val configCache: CacheManager<String, List<AiProviderConfig>>,
    private val encryptionService: EncryptionService
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
                    // Security audit S-07: the legacy field is stored encrypted;
                    // decrypt before exposing as the Apify token. Legacy plaintext
                    // values (pre-encryption) are read as-is once, then rewritten
                    // encrypted on the next update — a one-time migration, not a
                    // security downgrade.
                    apifyToken = decryptToken(it.geminiApiKey),
                    activeActorId = "default"
                )
            }
        }
    }

    /**
     * Decrypts an at-rest token. Falls back to the stored value ONLY for the
     * one-time migration of legacy plaintext written before encryption existed.
     * A genuine crypto outage (broken keyset) is logged at ERROR so it is not
     * silently mistaken for a migration case — the stored ciphertext would
     * simply fail provider auth downstream rather than leak.
     */
    private fun decryptToken(stored: String?): String {
        if (stored.isNullOrBlank()) return ""
        return runCatching { encryptionService.decrypt(stored) }
            .getOrElse { error ->
                // True ciphertext (Base64, >= 16 chars) failing to decrypt means
                // the keyset/master key is broken — surface loudly, keep value as-is
                // (it fails auth downstream; it is never *readable* plaintext).
                val looksLikeCiphertext = stored.length >= 16 &&
                    runCatching { android.util.Base64.decode(stored, android.util.Base64.DEFAULT) }.isSuccess
                if (looksLikeCiphertext) {
                    Timber.e(error, "geminiApiKey is ciphertext but decryption failed — keyset issue")
                } else {
                    Timber.w(error, "geminiApiKey not decryptable — one-time legacy plaintext migration")
                }
                stored
            }
    }

    override suspend fun updateScraperConfig(config: JobScraperConfig): CoreResult<Unit> = runCatchingCore {
        settingsLocalDataSource.updateGeminiApiKey(config.apifyToken)
    }
}
