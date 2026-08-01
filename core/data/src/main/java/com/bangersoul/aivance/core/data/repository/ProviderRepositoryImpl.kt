package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.database.dao.AiAnalyticsDao
import com.bangersoul.aivance.core.database.model.ProviderConfigurationEntity
import com.bangersoul.aivance.core.datastore.SecretsManager
import com.bangersoul.aivance.core.domain.repository.ProviderRepository
import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProviderRepositoryImpl @Inject constructor(
    private val aiAnalyticsDao: AiAnalyticsDao,
    private val secretsManager: SecretsManager,
    private val providerManager: com.bangersoul.aivance.sdk.infrastructure.ProviderManager
) : ProviderRepository {

    override fun getProviderConfigs(): Flow<List<ProviderConfiguration>> {
        return aiAnalyticsDao.getAllProviderConfigs().map { entities ->
            entities.map { entity ->
                // In a production app, we would fetch secrets reactively or on-demand
                // For this implementation, we use an empty map for secrets in the list flow
                // and fetch specifically in getProviderConfig.
                entity.toDomain(emptyMap())
            }
        }
    }

    override suspend fun getProviderConfig(id: String): ProviderConfiguration? {
        val entity = aiAnalyticsDao.getProviderConfig(id) ?: return null
        // Legacy fallback: configs saved before the multi-secret change only ever
        // stored "apiKey"; treat an absent marker as that legacy key so existing
        // users' credentials keep hydrating after the upgrade.
        val storedKeys = (entity.settings[SECRET_KEYS_SETTING] ?: "").split(",")
            .filter { it.isNotBlank() }
        val secretKeys = storedKeys.ifEmpty { listOf("apiKey") }
        val secrets = secretKeys.mapNotNull { key ->
            val value = secretsManager.getSecret("provider_${id}_${key}") ?: return@mapNotNull null
            if (value.isBlank()) null else key to value
        }.toMap()
        return entity.toDomain(secrets)
    }

    override suspend fun saveProviderConfig(config: ProviderConfiguration): Result<Unit> = runCatchingCore {
        aiAnalyticsDao.insertProviderConfig(config.toEntity())
        // Persist every secret keyed by its own field name (encrypted DataStore).
        config.secrets.forEach { (key, value) ->
            if (value.isNotBlank()) {
                secretsManager.saveSecret("provider_${config.providerId}_${key}", value)
            }
        }
        // Reconfigure the live DI-singleton provider so saved credentials take
        // effect immediately (previously only persisted, never applied).
        providerManager.reconfigure(config.providerId, config)
    }

    override suspend fun deleteProviderConfig(id: String): Result<Unit> = runCatchingCore {
        // Implementation for delete: remove every secret this provider holds.
        val entity = aiAnalyticsDao.getProviderConfig(id)
        val storedKeys = (entity?.settings?.get(SECRET_KEYS_SETTING) ?: "").split(",")
            .filter { it.isNotBlank() }
        val secretKeys = storedKeys.ifEmpty { listOf("apiKey") }
        secretKeys.forEach { key -> secretsManager.deleteSecret("provider_${id}_${key}") }
        providerManager.reconfigure(id, ProviderConfiguration(id))
    }

    private fun ProviderConfigurationEntity.toDomain(secrets: Map<String, String>): ProviderConfiguration {
        return ProviderConfiguration(
            providerId = provider,
            settings = (settings - SECRET_KEYS_SETTING) + mapOf(
                "type" to type,
                "selectedModel" to (selectedModel ?: ""),
                "actorId" to (actorId ?: ""),
                "isEnabled" to isEnabled.toString()
            ),
            secrets = secrets
        )
    }

    private fun ProviderConfiguration.toEntity(): ProviderConfigurationEntity {
        return ProviderConfigurationEntity(
            provider = providerId,
            type = settings["type"] ?: "AI",
            baseUrl = settings["baseUrl"],
            selectedModel = settings["selectedModel"],
            actorId = settings["actorId"],
            settings = (settings - setOf("type", "selectedModel", "actorId", "isEnabled")) +
                (SECRET_KEYS_SETTING to secrets.keys.joinToString(",")),
            isEnabled = settings["isEnabled"]?.toBoolean() ?: true
        )
    }

    companion object {
        /**
         * Reserved settings key that records which credential field keys a provider's
         * secrets map contains, so [getProviderConfig] can round-trip every secret
         * (not just a hardcoded "apiKey"). Stripped from the public settings map.
         */
        private const val SECRET_KEYS_SETTING = "__secretKeys"
    }
}
