package com.bangersoul.aivance.job.remoteok

import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.job.cache.JobCache
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
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
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

class RemoteOKProviderTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var jobCache: JobCache
    private lateinit var json: Json

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        jobCache = mockk(relaxed = true)
        json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `searchJobs successfully maps API response`() = runTest {
        val responseJson = """
            [
                {
                    "id": "rok-1",
                    "position": "React Developer",
                    "company": "WebCo",
                    "company_logo": "https://webco.com/logo.png",
                    "location": "Remote",
                    "salary_min": 90000,
                    "salary_max": 130000,
                    "date": "2026-07-15",
                    "url": "https://remoteok.com/remote-jobs/react-dev-1",
                    "description": "Build UIs with React.",
                    "tags": ["react", "typescript"]
                },
                {
                    "id": "rok-2",
                    "position": "Backend Engineer",
                    "company": "API Corp",
                    "location": "Remote",
                    "date": "2026-07-14",
                    "url": "https://remoteok.com/remote-jobs/backend-1",
                    "description": "Build APIs."
                }
            ]
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseJson))
        coEvery { jobCache.saveJobs(any()) } returns Unit

        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val provider = RemoteOKProvider(
            jobCache = jobCache,
            okHttpClient = client,
            baseRetrofit = retrofit,
            baseUrl = mockWebServer.url("/").toString()
        )
        provider.onInitialize()
        provider.onStart()

        val result = provider.searchJobs(JobSearchFilter(), JobSortOrder.RELEVANCE, 1)

        assertTrue("Expected Success, got $result", result is Result.Success)
        val jobs = (result as Result.Success).data
        assertEquals(2, jobs.size)
        assertEquals("React Developer", jobs[0].title)
        assertEquals("WebCo", jobs[0].company)
        assertEquals("https://webco.com/logo.png", jobs[0].companyLogoUrl)
        assertEquals(90000.0, jobs[0].salaryMin!!, 0.001)
        assertEquals(130000.0, jobs[0].salaryMax!!, 0.001)
        assertTrue(jobs[0].isRemote)
        assertEquals("remoteok", jobs[0].sourceProvider)
    }

    @Test
    fun `searchJobs returns failure on server error`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Error"))
        coEvery { jobCache.getJobs() } returns emptyList()

        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val provider = RemoteOKProvider(
            jobCache = jobCache,
            okHttpClient = client,
            baseRetrofit = retrofit,
            baseUrl = mockWebServer.url("/").toString()
        )
        provider.onInitialize()
        provider.onStart()

        val result = provider.searchJobs(JobSearchFilter(), JobSortOrder.RELEVANCE, 1)

        assertTrue("Expected Failure, got $result", result is Result.Failure)
    }

    @Test
    fun `searchJobs uses cached results when API fails`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(503))
        val cachedJobs = listOf(
            com.bangersoul.aivance.core.common.model.JobListing(
                id = "cached-1",
                title = "Cached Job",
                company = "Co",
                description = "",
                url = "",
                sourceProvider = "remoteok"
            )
        )
        coEvery { jobCache.getJobs() } returns cachedJobs

        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val provider = RemoteOKProvider(
            jobCache = jobCache,
            okHttpClient = client,
            baseRetrofit = retrofit,
            baseUrl = mockWebServer.url("/").toString()
        )
        provider.onInitialize()
        provider.onStart()

        val result = provider.searchJobs(JobSearchFilter(), JobSortOrder.RELEVANCE, 1)

        assertTrue("Expected Success from cache, got $result", result is Result.Success)
        assertEquals("Cached Job", (result as Result.Success).data[0].title)
    }
}
