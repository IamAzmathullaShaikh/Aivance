package com.bangersoul.aivance.sdk.config

import android.content.Context
import android.util.Base64
import com.bangersoul.aivance.sdk.security.AivanceSecurity
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SecretManagerTest {

    private lateinit var context: Context
    private lateinit var aead: Aead
    private lateinit var secretManager: SecretManagerImpl

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        aead = mockk()
        
        // Mock AivanceSecurity to return a keyset handle that returns our mock aead
        mockkObject(AivanceSecurity)
        val keysetHandle = mockk<KeysetHandle>()
        every { AivanceSecurity.getKeysetHandle(any()) } returns keysetHandle
        every { keysetHandle.getPrimitive(Aead::class.java) } returns aead

        // Mock android.util.Base64
        mockkStatic(Base64::class)
        every { Base64.encodeToString(any(), Base64.NO_WRAP) } answers {
            java.util.Base64.getEncoder().encodeToString(it.invocation.args[0] as ByteArray)
        }
        every { Base64.decode(any<String>(), Base64.NO_WRAP) } answers {
            java.util.Base64.getDecoder().decode(it.invocation.args[0] as String)
        }

        secretManager = SecretManagerImpl(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `encrypt should use aead and base64`() {
        val plainText = "hello"
        val cipherBytes = "encrypted".toByteArray()
        every { aead.encrypt(any(), null) } returns cipherBytes

        val result = secretManager.encrypt(plainText)
        
        // "encrypted" in base64 is "ZW5jcnlwdGVk"
        assertEquals("ZW5jcnlwdGVk", result)
    }

    @Test
    fun `decrypt should use aead and base64`() {
        val cipherText = "ZW5jcnlwdGVk" // "encrypted"
        val plainBytes = "hello".toByteArray()
        every { aead.decrypt(any(), null) } returns plainBytes

        val result = secretManager.decrypt(cipherText)
        
        assertEquals("hello", result)
    }
}
