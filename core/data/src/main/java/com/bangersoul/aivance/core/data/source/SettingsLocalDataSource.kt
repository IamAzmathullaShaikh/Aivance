package com.bangersoul.aivance.core.data.source

import com.bangersoul.aivance.core.datastore.UserPreferences
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface SettingsLocalDataSource {
    val userPreferences: Flow<UserPreferences>
    suspend fun updateGeminiApiKey(apiKey: String)
}

class SettingsLocalDataSourceImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : SettingsLocalDataSource {

    override val userPreferences: Flow<UserPreferences> = userPreferencesRepository.userPreferences

    override suspend fun updateGeminiApiKey(apiKey: String) {
        userPreferencesRepository.updateGeminiApiKey(apiKey)
    }
}
