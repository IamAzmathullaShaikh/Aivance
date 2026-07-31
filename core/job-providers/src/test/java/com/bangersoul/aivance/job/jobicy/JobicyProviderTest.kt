package com.bangersoul.aivance.job.jobicy

import com.bangersoul.aivance.job.cache.JobCache
import com.bangersoul.aivance.sdk.core.ProviderCapability
import io.mockk.mockk
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
 * Verifies the Retrofit interface parses the real Jobicy API schema
 * (verified against the live https://jobicy.com/api/v2/remote-jobs response).
 */
class JobicyProviderTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: JobicyApi

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
    fun `getJobs parses the live Jobicy schema`() = runTest {
        val json = """
        {
          "apiVersion": "2.2.15",
          "jobCount": 1,
          "jobs": [
            {
              "id": 147706,
              "url": "https://jobicy.com/jobs/147706-account-lead",
              "jobSlug": "147706-account-lead",
              "jobTitle": "Account Technical Lead",
              "companyName": "Autodesk",
              "companyLogo": "https://jobicy.com/logo.png",
              "jobIndustry": ["Technical Support"],
              "jobType": ["Full-Time"],
              "jobGeo": "APAC, Australia",
              "jobLevel": "Senior",
              "jobExcerpt": "Leads technical engagements.",
              "jobDescription": "<p>Full description</p>",
              "pubDate": "2026-07-30T19:45:05+00:00"
            }
          ]
        }
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(json))

        val response = api.getJobs(count = 100)

        assertTrue("Expected success but was code ${response.code()}", response.isSuccessful)
        val jobs = response.body()?.jobs ?: emptyList()
        assertEquals(1, jobs.size)
        val dto = jobs.first()
        assertEquals(147706L, dto.id)
        assertEquals("Account Technical Lead", dto.jobTitle)
        assertEquals("Autodesk", dto.companyName)
        assertEquals("APAC, Australia", dto.jobGeo)
        assertEquals("2026-07-30T19:45:05+00:00", dto.pubDate)
    }

    @Test
    fun `provider metadata is correct`() {
        val provider = JobicyProvider(
            jobCache = mockk<JobCache>(),
            okHttpClient = OkHttpClient(),
            retrofit = buildRetrofit(mockWebServer)
        )
        assertEquals("jobicy", provider.metadata.id)
        assertEquals("Jobicy", provider.metadata.name)
        assertTrue(provider.hasCapability(ProviderCapability.JobSearch))
    }

    private fun buildApi(server: MockWebServer): JobicyApi =
        buildRetrofit(server).create(JobicyApi::class.java)

    private fun buildRetrofit(server: MockWebServer): Retrofit {
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        return Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}
