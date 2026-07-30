package com.bangersoul.aivance.core.datastore

import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

class UserPreferencesSerializer @Inject constructor(
    private val cryptoManager: CryptoManager
) : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences = UserPreferences()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        return try {
            val decryptedBytes = cryptoManager.decrypt(input)
            if (decryptedBytes.isEmpty()) return defaultValue
            Json.decodeFromString(
                deserializer = UserPreferences.serializer(),
                string = decryptedBytes.decodeToString()
            )
        } catch (e: Exception) {
            defaultValue
        }
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        val bytes = Json.encodeToString(
            serializer = UserPreferences.serializer(),
            value = t
        ).encodeToByteArray()
        cryptoManager.encrypt(bytes, output)
    }
}
