package com.bangersoul.aivance.job.base

import io.mockk.every
import io.mockk.mockk
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class RetryInterceptorTest {

    private val testRequest = Request.Builder()
        .url("https://api.example.com/jobs")
        .build()

    private fun createResponse(code: Int, request: Request = testRequest): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code == 200) "OK" else "Error")
            .body("".toResponseBody("application/json".toMediaType()))
            .build()
    }

    @Test
    fun `successful request returns immediately without retry`() {
        val interceptor = RetryInterceptor(maxRetries = 3)
        var chainCalled = 0

        val chain = mockk<Interceptor.Chain>(relaxed = true)
        every { chain.request() } returns testRequest
        every { chain.proceed(any()) } answers {
            chainCalled++
            createResponse(200, firstArg())
        }

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals(1, chainCalled)
    }

    @Test
    fun `server error (500) is retried up to maxRetries`() {
        val interceptor = RetryInterceptor(maxRetries = 2, initialDelay = 1)
        var chainCalled = 0

        val chain = mockk<Interceptor.Chain>(relaxed = true)
        every { chain.request() } returns testRequest
        every { chain.proceed(any()) } answers {
            chainCalled++
            if (chainCalled <= 2) createResponse(500, firstArg()) else createResponse(200, firstArg())
        }

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals(3, chainCalled)
    }

    @Test
    fun `all retries exhausted returns last failure response`() {
        val interceptor = RetryInterceptor(maxRetries = 2, initialDelay = 1)
        var chainCalled = 0

        val chain = mockk<Interceptor.Chain>(relaxed = true)
        every { chain.request() } returns testRequest
        every { chain.proceed(any()) } answers {
            chainCalled++
            createResponse(502, firstArg())
        }

        val response = interceptor.intercept(chain)

        assertEquals(502, response.code)
        assertEquals(3, chainCalled)
    }

    @Test
    fun `client error 4xx (except 408 and 429) is not retried`() {
        val interceptor = RetryInterceptor(maxRetries = 3, initialDelay = 1)
        var chainCalled = 0

        val chain = mockk<Interceptor.Chain>(relaxed = true)
        every { chain.request() } returns testRequest
        every { chain.proceed(any()) } answers {
            chainCalled++
            createResponse(404, firstArg())
        }

        val response = interceptor.intercept(chain)

        assertEquals(404, response.code)
        assertEquals(1, chainCalled)
    }

    @Test
    fun `rate limiting 429 is retried`() {
        val interceptor = RetryInterceptor(maxRetries = 1, initialDelay = 1)
        var chainCalled = 0

        val chain = mockk<Interceptor.Chain>(relaxed = true)
        every { chain.request() } returns testRequest
        every { chain.proceed(any()) } answers {
            chainCalled++
            if (chainCalled == 1) createResponse(429, firstArg()) else createResponse(200, firstArg())
        }

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals(2, chainCalled)
    }

    @Test
    fun `request timeout 408 is retried`() {
        val interceptor = RetryInterceptor(maxRetries = 1, initialDelay = 1)
        var chainCalled = 0

        val chain = mockk<Interceptor.Chain>(relaxed = true)
        every { chain.request() } returns testRequest
        every { chain.proceed(any()) } answers {
            chainCalled++
            if (chainCalled == 1) createResponse(408, firstArg()) else createResponse(200, firstArg())
        }

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals(2, chainCalled)
    }

    @Test
    fun `IO exceptions are retried`() {
        val interceptor = RetryInterceptor(maxRetries = 2, initialDelay = 1)
        var chainCalled = 0

        val chain = mockk<Interceptor.Chain>(relaxed = true)
        every { chain.request() } returns testRequest
        every { chain.proceed(any()) } answers {
            chainCalled++
            throw IOException("Connection reset by peer")
        }

        val exception = assertThrows(IOException::class.java) {
            interceptor.intercept(chain)
        }

        // The retry interceptor re-throws the original IOException after all retries are exhausted
        assertEquals("Connection reset by peer", exception.message)
        assertEquals(3, chainCalled)
    }

    @Test
    fun `exception is thrown if all IO retries fail`() {
        val interceptor = RetryInterceptor(maxRetries = 1, initialDelay = 1)

        val chain = mockk<Interceptor.Chain>(relaxed = true)
        every { chain.request() } returns testRequest
        every { chain.proceed(any()) } throws IOException("Network failure")

        val exception = assertThrows(IOException::class.java) {
            interceptor.intercept(chain)
        }

        assertEquals("Network failure", exception.message)
    }
}
