package com.bangersoul.aivance.core.datastore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CryptoManager @Inject constructor() {

    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM, KEYSTORE_PROVIDER).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    fun encrypt(bytes: ByteArray, outputStream: OutputStream) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val iv = cipher.iv
        outputStream.write(iv.size)
        outputStream.write(iv)
        val encryptedBytes = cipher.doFinal(bytes)
        outputStream.write(encryptedBytes.size ushr 24)
        outputStream.write(encryptedBytes.size ushr 16)
        outputStream.write(encryptedBytes.size ushr 8)
        outputStream.write(encryptedBytes.size)
        outputStream.write(encryptedBytes)
    }

    fun decrypt(inputStream: InputStream): ByteArray {
        val ivSize = inputStream.read()
        if (ivSize <= 0) return byteArrayOf()
        val iv = ByteArray(ivSize)
        inputStream.read(iv)

        val b1 = inputStream.read()
        val b2 = inputStream.read()
        val b3 = inputStream.read()
        val b4 = inputStream.read()
        if (b1 < 0 || b2 < 0 || b3 < 0 || b4 < 0) return byteArrayOf()
        val encryptedSize = (b1 shl 24) or (b2 shl 16) or (b3 shl 8) or b4
        if (encryptedSize <= 0) return byteArrayOf()

        val encryptedBytes = ByteArray(encryptedSize)
        var totalRead = 0
        while (totalRead < encryptedSize) {
            val read = inputStream.read(encryptedBytes, totalRead, encryptedSize - totalRead)
            if (read < 0) break
            totalRead += read
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(encryptedBytes)
    }

    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "aivance_encrypted_datastore_key"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    }
}
