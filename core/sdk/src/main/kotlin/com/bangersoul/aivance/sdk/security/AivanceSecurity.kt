package com.bangersoul.aivance.sdk.security

import android.content.Context
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.config.TinkConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.io.IOException
import java.security.GeneralSecurityException

/**
 * Handles Tink initialization and security-related operations like key management.
 */
object AivanceSecurity {
    private const val KEYSET_NAME = "aivance_keyset"
    private const val PREF_FILE_NAME = "aivance_security_prefs"
    private const val MASTER_KEY_URI = "android-keystore://aivance_master_key"

    /**
     * Initializes Tink and registers configurations.
     */
    fun init(context: Context) {
        try {
            TinkConfig.register()
        } catch (e: GeneralSecurityException) {
            throw RuntimeException("Failed to initialize Tink", e)
        }
    }

    /**
     * Returns a [KeysetHandle] managed by Android Keystore.
     * It automatically generates a new key if it doesn't exist.
     */
    @Throws(GeneralSecurityException::class, IOException::class)
    fun getKeysetHandle(context: Context): KeysetHandle {
        return AndroidKeysetManager.Builder()
            .withSharedPref(context, KEYSET_NAME, PREF_FILE_NAME)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
    }

}
