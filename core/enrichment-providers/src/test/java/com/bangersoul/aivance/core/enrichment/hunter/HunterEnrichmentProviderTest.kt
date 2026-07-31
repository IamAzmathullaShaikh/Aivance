package com.bangersoul.aivance.core.enrichment.hunter

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Verifies the Hunter.io Retrofit interface parses the real API schemas
 * (v2/domain-search and v2/email-verifier) against MockWebServer.
 *
 * Note: HunterEnrichmentProvider rebuilds its Retrofit with the production
 * base URL, so wire-format verification happens at the HunterApi level.
 */
class HunterEnrichmentProviderTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: HunterApi

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        api = buildApi(mockWebServer)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `domainSearch parses the real domain-search schema`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {
                  "data": {
                    "domain": "stripe.com",
                    "organization": "Stripe",
                    "pattern": "{first}@stripe.com",
                    "emails": [
                      {
                        "value": "jane@stripe.com",
                        "type": "personal",
                        "confidence": 98,
                        "first_name": "Jane",
                        "last_name": "Doe",
                        "position": "VP Engineering",
                        "linkedin": "https://www.linkedin.com/in/jane",
                        "phone_number": null
                      }
                    ]
                  }
                }
                """.trimIndent()
            )
        )

        val response = api.domainSearch("stripe.com", "test-key")

        assertTrue("Expected success but was code ${response.code()}", response.isSuccessful)
        val emails = response.body()?.data?.emails ?: emptyList()
        assertEquals(1, emails.size)
        val email = emails.first()
        assertEquals("jane@stripe.com", email.value)
        assertEquals("Jane", email.firstName)
        assertEquals("Doe", email.lastName)
        assertEquals("VP Engineering", email.position)
        assertEquals(98, email.confidence)
    }

    @Test
    fun `emailVerifier parses the real email-verifier schema`() = runTest {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {
                  "data": {
                    "status": "valid",
                    "result": "deliverable",
                    "score": 98,
                    "regexp": true,
                    "disposable": false
                  }
                }
                """.trimIndent()
            )
        )

        val response = api.verifyEmail("jane@stripe.com", "test-key")

        assertTrue("Expected success but was code ${response.code()}", response.isSuccessful)
        val data = response.body()?.data
        assertEquals("valid", data?.status)
        assertEquals("deliverable", data?.result)
        assertEquals(98, data?.score)
    }

    @Test
    fun `missing API key returns Failure without network`() = runTest {
        val provider = HunterEnrichmentProvider(
            config = ProviderConfiguration(
                providerId = "hunter",
                settings = emptyMap(),
                secrets = mapOf("apiKey" to "")
            ),
            baseOkHttpClient = OkHttpClient(),
            baseRetrofit = buildRetrofit(mockWebServer)
        )

        val recruiters = provider.findRecruiters("stripe.com")
        val verified = provider.verifyEmail("jane@stripe.com")

        assertTrue("Expected Failure but was $recruiters", recruiters is Result.Failure)
        assertTrue("Expected Failure but was $verified", verified is Result.Failure)
        assertEquals(0, mockWebServer.requestCount)
    }

    private fun buildApi(server: MockWebServer): HunterApi =
        buildRetrofit(server).create(HunterApi::class.java)

    private fun buildRetrofit(server: MockWebServer): Retrofit {
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        return Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
