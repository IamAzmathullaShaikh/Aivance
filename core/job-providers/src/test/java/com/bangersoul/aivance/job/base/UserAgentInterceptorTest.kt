package com.bangersoul.aivance.job.base

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class UserAgentInterceptorTest {

    private val interceptor = UserAgentInterceptor()

    @Test
    fun `adds User-Agent header to request`() {
        val requestSlot = slot<Request>()

        val chain = mockk<Interceptor.Chain>(relaxed = true)
        val originalRequest = Request.Builder()
            .url("https://api.example.com/jobs")
            .build()
        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(requestSlot)) } answers {
            Response.Builder()
                .request(requestSlot.captured)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("".toResponseBody("application/json".toMediaType()))
                .build()
        }

        interceptor.intercept(chain)

        assertEquals("Aivance/1.0.0 (Android)", requestSlot.captured.header("User-Agent"))
    }

    @Test
    fun `preserves original request headers`() {
        val requestSlot = slot<Request>()

        val chain = mockk<Interceptor.Chain>(relaxed = true)
        val originalRequest = Request.Builder()
            .url("https://api.example.com/jobs")
            .header("Authorization", "Bearer test-token")
            .build()
        every { chain.request() } returns originalRequest
        every { chain.proceed(capture(requestSlot)) } answers {
            Response.Builder()
                .request(requestSlot.captured)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("".toResponseBody("application/json".toMediaType()))
                .build()
        }

        interceptor.intercept(chain)

        assertEquals("Bearer test-token", requestSlot.captured.header("Authorization"))
        assertEquals("Aivance/1.0.0 (Android)", requestSlot.captured.header("User-Agent"))
    }
}
