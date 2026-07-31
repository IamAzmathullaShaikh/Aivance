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
    private val secretsManager: SecretsManager
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
        val apiKey = secretsManager.getSecret("provider_${id}_apiKey") ?: ""
        return entity.toDomain(mapOf("apiKey" to apiKey))
    }

    override suspend fun saveProviderConfig(config: ProviderConfiguration): Result<Unit> = runCatchingCore {
        aiAnalyticsDao.insertProviderConfig(config.toEntity())
        config.secrets["apiKey"]?.let {
            secretsManager.saveSecret("provider_${config.providerId}_apiKey", it)
        }
    }

    override suspend fun deleteProviderConfig(id: String): Result<Unit> = runCatchingCore {
        // Implementation for delete
        secretsManager.deleteSecret("provider_${id}_apiKey")
    }

    private fun ProviderConfigurationEntity.toDomain(secrets: Map<String, String>): ProviderConfiguration {
        return ProviderConfiguration(
            providerId = provider,
            settings = settings + mapOf(
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
            settings = settings - setOf("type", "selectedModel", "actorId", "isEnabled"),
            isEnabled = settings["isEnabled"]?.toBoolean() ?: true
        )
    }
}
