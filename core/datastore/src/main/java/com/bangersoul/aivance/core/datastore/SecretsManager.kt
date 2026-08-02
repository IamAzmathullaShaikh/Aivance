package com.bangersoul.aivance.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bangersoul.aivance.core.database.security.EncryptionService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.secretsDataStore by preferencesDataStore(name = "aivance_secrets")

/**
 * Manages sensitive user data like API keys using encrypted storage.
 */
@Singleton
class SecretsManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionService: EncryptionService
) {
    private val dataStore = context.secretsDataStore

    suspend fun saveSecret(key: String, value: String) {
        val encrypted = encryptionService.encrypt(value)
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey(key)] = encrypted
        }
    }

    suspend fun getSecret(key: String): String? {
        val encrypted = dataStore.data.first()[stringPreferencesKey(key)] ?: return null
        return encryptionService.decrypt(encrypted)
    }

    suspend fun deleteSecret(key: String) {
        dataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey(key))
        }
    }

    suspend fun clearAllSecrets() {
        dataStore.edit { it.clear() }
    }
}
