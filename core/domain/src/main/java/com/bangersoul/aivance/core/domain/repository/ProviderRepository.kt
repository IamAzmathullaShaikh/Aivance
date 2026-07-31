package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing provider configurations.
 */
interface ProviderRepository {
    fun getProviderConfigs(): Flow<List<ProviderConfiguration>>
    suspend fun getProviderConfig(id: String): ProviderConfiguration?
    suspend fun saveProviderConfig(config: ProviderConfiguration): Result<Unit>
    suspend fun deleteProviderConfig(id: String): Result<Unit>
}
