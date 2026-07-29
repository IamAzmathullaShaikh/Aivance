package com.bangersoul.aivance.core.datastore

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface UserPreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun updateGeminiApiKey(apiKey: String)
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
}
