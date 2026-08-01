package com.bangersoul.aivance.job.greenhouse

import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.enums.RemoteType
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.job.cache.JobCache
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json

class GreenhouseProviderTest {

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
    fun `searchJobs maps and filters results by query`() = runTest {
        val responseJson = """
            {
                "jobs": [
                    {
                        "id": 1001,
                        "title": "Senior Android Engineer",
                        "location": {"name": "San Francisco, CA"},
                        "absolute_url": "https://boards.greenhouse.io/jobs/1001",
                        "updated_at": "2026-07-20T12:00:00+00:00",
                        "content": "<p>Build Android apps</p>"
                    },
                    {
                        "id": 1002,
                        "title": "iOS Developer",
                        "location": {"name": "New York, NY"},
                        "absolute_url": "https://boards.greenhouse.io/jobs/1002",
                        "updated_at": "2026-07-19T10:00:00+00:00",
                        "content": "Build iOS apps."
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseJson))
        coEvery { jobCache.saveJobs(any()) } returns Unit

        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val provider = GreenhouseProvider(
            boardToken = "testcorp",
            jobCache = jobCache,
            okHttpClient = client,
            baseRetrofit = retrofit,
            baseUrl = mockWebServer.url("/").toString()
        )
        provider.onInitialize()
        provider.onStart()

        // Filter by "Android" keyword
        val result = provider.searchJobs(
            JobSearchFilter(query = "Android"),
            JobSortOrder.RELEVANCE,
            1
        )

        assertTrue("Expected Success, got $result", result is Result.Success)
        val jobs = (result as Result.Success).data
        assertEquals(1, jobs.size)
        assertEquals("Senior Android Engineer", jobs[0].title)
        assertEquals("Testcorp", jobs[0].company) // boardToken first char uppercased
        assertEquals("San Francisco, CA", jobs[0].location)
        assertEquals(RemoteType.ON_SITE, jobs[0].remoteType)
        assertFalse(jobs[0].isRemote)
    }

    @Test
    fun `searchJobs returns all jobs when no filter`() = runTest {
        val responseJson = """
            {
                "jobs": [
                    {
                        "id": 2001,
                        "title": "Engineer",
                        "location": {"name": "Remote"},
                        "absolute_url": "https://boards.greenhouse.io/jobs/2001"
                    }
                ]
            }
        """.trimIndent()

        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseJson))
        coEvery { jobCache.saveJobs(any()) } returns Unit

        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val provider = GreenhouseProvider(
            boardToken = "remoteorg",
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
        assertEquals(1, jobs.size)
        assertEquals("Engineer", jobs[0].title)
        assertEquals(RemoteType.REMOTE, jobs[0].remoteType)
        assertTrue(jobs[0].isRemote)
    }
}
