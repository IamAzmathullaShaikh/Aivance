package com.bangersoul.aivance.core.network.security

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CertificatePinningInterceptorTest {

    private val baseRequest: Request = Request.Builder()
        .url("https://example.com/jobs")
        .build()

    private val pinnedRequest: Request = Request.Builder()
        .url("https://api.groq.com/openai/v1/models")
        .build()

    private fun responseFor(request: Request): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(ResponseBody.create(null, ""))
            .build()

    private fun chainFor(request: Request): Interceptor.Chain {
        val response = responseFor(request)
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.connection() } returns null
        every { chain.proceed(request) } returns response
        return chain
    }

    @Test
    fun `placeholder pins are detected`() {
        val blank = CertificatePinningInterceptor.PinEntry("a.com", "")
        assertTrue(blank.isPlaceholder)

        val addYour = CertificatePinningInterceptor.PinEntry("a.com", "ADD_YOUR_PIN_HERE")
        assertTrue(addYour.isPlaceholder)

        val replaceWith = CertificatePinningInterceptor.PinEntry("a.com", "REPLACE_WITH_REAL_PIN")
        assertTrue(replaceWith.isPlaceholder)

        val allAs = CertificatePinningInterceptor.PinEntry("a.com", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        assertTrue(allAs.isPlaceholder)

        val real = CertificatePinningInterceptor.PinEntry("a.com", "c7e3f89025e1a38f7f4d2a10b9e8d7c6b5a4f3e2d1c0b9a8f7e6d5c4b3a2f1e0")
        assertFalse(real.isPlaceholder)
    }

    @Test
    fun `host without pins passes through`() {
        val interceptor = CertificatePinningInterceptor(listOf())
        val chain = chainFor(baseRequest)
        val response = interceptor.intercept(chain)
        assertEquals(200, response.code)
        verify { chain.proceed(baseRequest) }
    }

    @Test
    fun `host with only placeholder pins passes through`() {
        val interceptor = CertificatePinningInterceptor(
            listOf(
                CertificatePinningInterceptor.PinEntry("example.com", "REPLACE_WITH_REAL_PIN"),
                CertificatePinningInterceptor.PinEntry("example.com", "ADD_YOUR_PIN")
            )
        )
        val chain = chainFor(baseRequest)
        val response = interceptor.intercept(chain)
        assertEquals(200, response.code)
        verify { chain.proceed(baseRequest) }
    }

    @Test
    fun `host without a matching pin entry passes through even with other pins`() {
        // Pins exist but only for api.groq.com; example.com is untouched.
        val interceptor = CertificatePinningInterceptor(CertificatePinningInterceptor.DEFAULT_PINS)
        val chain = chainFor(baseRequest)
        val response = interceptor.intercept(chain)
        assertEquals(200, response.code)
        verify { chain.proceed(baseRequest) }
    }

    @Test
    fun `pinned host without TLS connection passes through`() {
        val interceptor = CertificatePinningInterceptor(CertificatePinningInterceptor.DEFAULT_PINS)
        // The mocked chain returns a null connection, so pinning is skipped.
        val chain = chainFor(pinnedRequest)
        val response = interceptor.intercept(chain)
        assertEquals(200, response.code)
        verify { chain.proceed(pinnedRequest) }
    }

    @Test
    fun `default pins cover expected providers`() {
        val hosts = CertificatePinningInterceptor.DEFAULT_PINS.map { it.hostname }.toSet()
        assertTrue(hosts.contains("api.groq.com"))
        assertTrue(hosts.contains("api.openai.com"))
        assertTrue(hosts.contains("openrouter.ai"))
        assertTrue(hosts.contains("remoteok.com"))
        assertTrue(hosts.contains("remotive.com"))
        assertTrue(hosts.contains("api.apify.com"))
    }

    @Test
    fun `pins are validated against placeholder prefix contract`() {
        // Guard: real-looking pins must not be flagged as placeholders.
        val sample = CertificatePinningInterceptor.DEFAULT_PINS.first { it.hostname == "api.groq.com" }
        assertFalse(sample.isPlaceholder)
        assertTrue(sample.sha256Hash.length >= 64)
    }
}
