package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.model.JobScraperConfig
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getAiProviderConfigs(): Flow<CoreResult<List<AiProviderConfig>>>
    suspend fun updateAiProviderConfig(config: AiProviderConfig): CoreResult<Unit>
    fun getScraperConfig(): Flow<CoreResult<JobScraperConfig>>
    suspend fun updateScraperConfig(config: JobScraperConfig): CoreResult<Unit>
}
