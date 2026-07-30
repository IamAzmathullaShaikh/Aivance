package com.bangersoul.aivance.core.datastore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.test.runTest

@RunWith(RobolectricTestRunner::class)
class UserPreferencesSerializerTest {

    private val cryptoManager = CryptoManager()
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
