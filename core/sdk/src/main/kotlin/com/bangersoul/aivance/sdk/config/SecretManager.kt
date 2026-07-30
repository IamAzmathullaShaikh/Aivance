package com.bangersoul.aivance.sdk.config

import android.content.Context
import android.util.Base64
import com.bangersoul.aivance.sdk.security.AivanceSecurity
import com.google.crypto.tink.Aead
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface for managing sensitive configuration secrets.
 */
interface SecretManager {
    /**
     * Encrypts the given plain text.
     * @param plainText The text to encrypt.
     * @return The Base64 encoded cipher text.
     */
    fun encrypt(plainText: String): String

    /**
     * Decrypts the given cipher text.
     * @param cipherText The Base64 encoded cipher text.
     * @return The decrypted plain text.
     */
    fun decrypt(cipherText: String): String
}

/**
 * Implementation of [SecretManager] using Google Tink and Android Keystore.
 */
@Singleton
class SecretManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SecretManager {

    private val aead: Aead by lazy {
        AivanceSecurity.getKeysetHandle(context).getPrimitive(Aead::class.java)
    }

    override fun encrypt(plainText: String): String {
        return try {
            val ciphertext = aead.encrypt(plainText.toByteArray(Charsets.UTF_8), null)
            Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw RuntimeException("Encryption failed", e)
        }
    }

    override fun decrypt(cipherText: String): String {
        return try {
            val decoded = Base64.decode(cipherText, Base64.NO_WRAP)
            val decrypted = aead.decrypt(decoded, null)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            throw RuntimeException("Decryption failed", e)
        }
    }
}
