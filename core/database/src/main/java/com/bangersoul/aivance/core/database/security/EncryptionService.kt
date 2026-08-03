package com.bangersoul.aivance.core.database.security

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tink + AndroidKeyStore AEAD wrapper for at-rest secrets.
 *
 * **Fail-closed by design**: if the keyset/master key is unavailable, or a
 * crypto operation fails, [encrypt]/[decrypt] throw instead of silently
 * degrading to plaintext. A plaintext fallback would defeat the entire
 * purpose of at-rest encryption (secrets must never be stored unencrypted
 * just because key initialization failed).
 */
@Singleton
class EncryptionService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var aead: Aead? = null

    init {
        initialize()
    }

    @Synchronized
    private fun initialize() {
        if (aead != null) return
        try {
            AeadConfig.register()
            val keysetName = "aivance_keyset"
            val prefFileName = "aivance_tink_prefs"
            val masterKeyUri = "android-keystore://aivance_master_key"

            aead = AndroidKeysetManager.Builder()
                .withSharedPref(context, keysetName, prefFileName)
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri(masterKeyUri)
                .build()
                .keysetHandle
                .getPrimitive(Aead::class.java)
            Log.d("EncryptionService", "Tink initialized successfully")
        } catch (e: Exception) {
            Log.e("EncryptionService", "Failed to initialize Tink — encryption will fail closed", e)
        }
    }

    private fun requireAead(): Aead {
        if (aead == null) initialize()
        return aead ?: throw IllegalStateException(
            "Tink/AEAD unavailable — refusing to operate on secrets without encryption"
        )
    }

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        val currentAead = requireAead()
        return try {
            val ciphertext = currentAead.encrypt(plainText.toByteArray(Charsets.UTF_8), null)
            Base64.encodeToString(ciphertext, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("EncryptionService", "Encryption failed — failing closed", e)
            throw IllegalStateException("Encryption failed; refusing to store plaintext", e)
        }
    }

    fun decrypt(cipherText: String): String {
        if (cipherText.isEmpty()) return ""
        val currentAead = requireAead()
        return try {
            val ciphertextBytes = Base64.decode(cipherText, Base64.DEFAULT)
            val decrypted = currentAead.decrypt(ciphertextBytes, null)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("EncryptionService", "Decryption failed — failing closed", e)
            throw IllegalStateException("Decryption failed; refusing to return data as-is", e)
        }
    }
}
