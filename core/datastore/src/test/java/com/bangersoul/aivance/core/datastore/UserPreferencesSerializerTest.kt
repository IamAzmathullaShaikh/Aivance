package com.bangersoul.aivance.core.datastore

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.test.runTest

class UserPreferencesSerializerTest {

    // The production CryptoManager is backed by the AndroidKeyStore provider, which cannot
    // generate keys under Robolectric. Substitute a reversible pass-through double so the
    // serializer's wire format (JSON -> encrypt -> decrypt -> JSON) is what gets tested.
    private val cryptoManager = mockk<CryptoManager>().apply {
        every { encrypt(any(), any()) } answers {
            secondArg<OutputStream>().write(firstArg<ByteArray>())
        }
        every { decrypt(any()) } answers {
            firstArg<InputStream>().readBytes()
        }
    }
    private val serializer = UserPreferencesSerializer(cryptoManager)

    @Test
    fun serializer_defaultValues_areCorrect() {
        assertEquals(false, serializer.defaultValue.onboardingCompleted)
        assertEquals(ThemeConfig.FOLLOW_SYSTEM, serializer.defaultValue.themeConfig)
        assertEquals(null, serializer.defaultValue.geminiApiKey)
    }

    @Test
    fun writeAndRead_encryptedUserPreferences_roundTripsSuccessfully() = runTest {
        val userPrefs = UserPreferences(
            onboardingCompleted = true,
            themeConfig = ThemeConfig.DARK,
            geminiApiKey = "test_encrypted_api_key_12345"
        )

        val outputStream = ByteArrayOutputStream()
        serializer.writeTo(userPrefs, outputStream)

        val inputStream = ByteArrayInputStream(outputStream.toByteArray())
        val readResult = serializer.readFrom(inputStream)

        assertNotNull(readResult)
        assertEquals(true, readResult.onboardingCompleted)
        assertEquals(ThemeConfig.DARK, readResult.themeConfig)
        assertEquals("test_encrypted_api_key_12345", readResult.geminiApiKey)
    }
}
