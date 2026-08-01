package com.bangersoul.aivance.core.datastore

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun updateGeminiApiKey(apiKey: String)
    suspend fun updateOnboardingCompleted(completed: Boolean)
    suspend fun updateThemeConfig(themeConfig: ThemeConfig)
    suspend fun updateAccentSeed(accentSeed: String)
    suspend fun updateDynamicColor(enabled: Boolean)
}

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<UserPreferences>
) : UserPreferencesRepository {

    override val userPreferences: Flow<UserPreferences> = dataStore.data

    override suspend fun updateGeminiApiKey(apiKey: String) {
        dataStore.updateData {
            it.copy(geminiApiKey = apiKey)
        }
    }

    override suspend fun updateOnboardingCompleted(completed: Boolean) {
        dataStore.updateData {
            it.copy(onboardingCompleted = completed)
        }
    }

    override suspend fun updateThemeConfig(themeConfig: ThemeConfig) {
        dataStore.updateData {
            it.copy(themeConfig = themeConfig)
        }
    }

    override suspend fun updateAccentSeed(accentSeed: String) {
        dataStore.updateData {
            it.copy(accentSeed = accentSeed)
        }
    }

    override suspend fun updateDynamicColor(enabled: Boolean) {
        dataStore.updateData {
            it.copy(dynamicColor = enabled)
        }
    }
}
