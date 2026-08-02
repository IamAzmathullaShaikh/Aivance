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

@Singleton
class EncryptionService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var aead: Aead? = null

    init {
        try {
            Log.d("EncryptionService", "Initializing Tink...")
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
            Log.e("EncryptionService", "Failed to initialize Tink", e)
        }
    }

    fun encrypt(plainText: String): String {
        val currentAead = aead ?: return plainText
        if (plainText.isEmpty()) return ""
        return try {
            val ciphertext = currentAead.encrypt(plainText.toByteArray(Charsets.UTF_8), null)
            Base64.encodeToString(ciphertext, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("EncryptionService", "Encryption failed", e)
            plainText
        }
    }

    fun decrypt(cipherText: String): String {
        val currentAead = aead ?: return cipherText
        if (cipherText.isEmpty()) return ""
        return try {
            val ciphertextBytes = Base64.decode(cipherText, Base64.DEFAULT)
            val decrypted = currentAead.decrypt(ciphertextBytes, null)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("EncryptionService", "Decryption failed", e)
            cipherText
        }
    }
}
