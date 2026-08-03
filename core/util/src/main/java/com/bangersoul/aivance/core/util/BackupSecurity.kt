package com.bangersoul.aivance.core.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backup file encryption.
 *
 * Security fixes over the previous implementation (audit S-04):
 * - **No hardcoded passphrase.** The default passphrase is a random 256-bit
 *   secret generated on first use, wrapped by an AndroidKeyStore AES-GCM key
 *   and stored only as ciphertext. Nothing in source code can decrypt backups.
 * - **Random per-file salt.** Each export uses a fresh 16-byte salt written to
 *   the file header, eliminating the old fixed-salt weakness.
 * - **600,000 PBKDF2-HMAC-SHA256 iterations** (OWASP 2023 recommendation for
 *   PBKDF2), up from 10,000.
 *
 * File format: `AVB1` magic (4) + salt (16) + IV (12) + AES-256-GCM ciphertext.
 */
@Singleton
class BackupSecurity @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Returns the device-bound backup passphrase, generating and wrapping it on
     * first use. Same-device export/import work seamlessly; restoring a backup
     * on a *new* device requires the caller to supply the passphrase explicitly
     * (documented known limitation — no passphrase UI exists yet).
     */
    fun devicePassphrase(): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val wrapped = prefs.getString(KEY_WRAPPED_PASSPHRASE, null)
        if (wrapped != null) {
            runCatching { return unwrap(wrapped) }.onFailure { e ->
                // The AndroidKeyStore wrap key was invalidated (device restore,
                // data wipe, etc.). Regenerating means every previously exported
                // backup becomes unrecoverable — this must be loud, not silent.
                Timber.e(e, "Backup device passphrase wrap invalidated — regenerating; old exports unrecoverable")
            }
        }
        val fresh = generatePassphrase()
        prefs.edit().putString(KEY_WRAPPED_PASSPHRASE, wrap(fresh)).apply()
        return fresh
    }

    // ── AndroidKeyStore wrapping ─────────────────────────────────────────

    private fun wrap(passphrase: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(passphrase.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + ciphertext, Base64.NO_WRAP)
    }

    private fun unwrap(wrapped: String): String {
        val raw = Base64.decode(wrapped, Base64.NO_WRAP)
        val iv = raw.copyOfRange(0, 12)
        val ciphertext = raw.copyOfRange(12, raw.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey
        return KeyGenerator.getInstance(ALGORITHM, KEYSTORE_PROVIDER).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    // ── Backup file crypto (stateless helpers, usable from tests) ────────

    companion object {
        const val BACKUP_MAGIC = "AVB1"
        private const val PBKDF2_ITERATIONS = 600_000
        private const val KEY_SIZE_BITS = 256
        private const val SALT_SIZE = 16
        private const val IV_SIZE = 12

        private const val PREFS_NAME = "aivance_backup_secure"
        private const val KEY_WRAPPED_PASSPHRASE = "wrapped_passphrase"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "aivance_backup_key"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

        private fun generatePassphrase(): String {
            val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
            return Base64.encodeToString(bytes, Base64.NO_WRAP)
        }

        /** Encrypts [plainText] under [passphrase]; returns the AVB1 file bytes. */
        fun encryptString(plainText: String, passphrase: String): ByteArray {
            val salt = ByteArray(SALT_SIZE).also { SecureRandom().nextBytes(it) }
            val keySpec = deriveKey(passphrase, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(IV_SIZE).also { SecureRandom().nextBytes(it) }
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, GCMParameterSpec(128, iv))
            val ciphertext = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            return ByteArrayOutputStream().use { out ->
                out.write(BACKUP_MAGIC.toByteArray(Charsets.US_ASCII))
                out.write(salt)
                out.write(iv)
                out.write(ciphertext)
                out.toByteArray()
            }
        }

        /** Decrypts AVB1 file bytes under [passphrase]; throws on bad magic/key. */
        fun decryptBytes(encryptedData: ByteArray, passphrase: String): String {
            require(encryptedData.size > SALT_SIZE + IV_SIZE) { "Backup file too short" }
            val magic = String(encryptedData.copyOfRange(0, 4), Charsets.US_ASCII)
            require(magic == BACKUP_MAGIC) { "Unrecognized backup format (expected $BACKUP_MAGIC)" }
            val salt = encryptedData.copyOfRange(4, 4 + SALT_SIZE)
            val iv = encryptedData.copyOfRange(4 + SALT_SIZE, 4 + SALT_SIZE + IV_SIZE)
            val ciphertext = encryptedData.copyOfRange(4 + SALT_SIZE + IV_SIZE, encryptedData.size)

            val keySpec = deriveKey(passphrase, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec, GCMParameterSpec(128, iv))
            val decrypted = cipher.doFinal(ciphertext)
            return String(decrypted, Charsets.UTF_8)
        }

        private fun deriveKey(passphrase: String, salt: ByteArray): SecretKeySpec {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(passphrase.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE_BITS)
            return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
        }
    }
}
