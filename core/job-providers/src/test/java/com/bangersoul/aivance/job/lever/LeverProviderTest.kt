package com.bangersoul.aivance.job.lever

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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json

class LeverProviderTest {

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
    fun `searchJobs maps and filters Lever postings`() = runTest {
        val responseJson = """
            [
                {
                    "id": "lev-1",
                    "text": "Senior Backend Engineer",
                    "categories": {
                        "location": "San Francisco, CA",
                        "commitment": "full-time",
                        "team": "Engineering"
                    },
                    "applyUrl": "https://jobs.lever.co/lev-1",
                    "createdAt": 1750000000000,
                    "description": "Build distributed systems.",
                    "descriptionHtml": "<p>Build distributed systems.</p>"
                },
                {
                    "id": "lev-2",
                    "text": "Product Designer",
                    "categories": {
                        "location": "Remote",
                        "commitment": "contract",
                        "team": "Design"
                    },
                    "applyUrl": "https://jobs.lever.co/lev-2",
                    "createdAt": 1749000000000,
                    "description": "Design great experiences."
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

        val provider = LeverProvider(
            companyId = "acmecorp",
            jobCache = jobCache,
            okHttpClient = client,
            baseRetrofit = retrofit,
            baseUrl = mockWebServer.url("/").toString()
        )
        provider.onInitialize()
        provider.onStart()

        val result = provider.searchJobs(
            JobSearchFilter(query = "Backend"),
            JobSortOrder.RELEVANCE,
            1
        )

        assertTrue("Expected Success, got $result", result is Result.Success)
        val jobs = (result as Result.Success).data
        assertEquals(1, jobs.size)
        assertEquals("Senior Backend Engineer", jobs[0].title)
        assertEquals("Acmecorp", jobs[0].company)
        assertEquals(com.bangersoul.aivance.core.common.enums.EmploymentType.FULL_TIME, jobs[0].employmentType)
        assertEquals(RemoteType.ON_SITE, jobs[0].remoteType)
    }

    @Test
    fun `searchJobs filters by location`() = runTest {
        val responseJson = """
            [
                {
                    "id": "lev-3",
                    "text": "Remote Dev",
                    "categories": {
                        "location": "Remote",
                        "commitment": "full-time"
                    },
                    "createdAt": null,
                    "description": "Work from anywhere."
                },
                {
                    "id": "lev-4",
                    "text": "Office Dev",
                    "categories": {
                        "location": "Austin, TX",
                        "commitment": "full-time"
                    },
                    "createdAt": null,
                    "description": "Work from office."
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

        val provider = LeverProvider(
            companyId = "startup",
            jobCache = jobCache,
            okHttpClient = client,
            baseRetrofit = retrofit,
            baseUrl = mockWebServer.url("/").toString()
        )
        provider.onInitialize()
        provider.onStart()

        // Filter by "Austin" location - should find Office Dev
        val result = provider.searchJobs(
            JobSearchFilter(location = "Austin"),
            JobSortOrder.RELEVANCE,
            1
        )

        assertTrue("Expected Success, got $result", result is Result.Success)
        val jobs = (result as Result.Success).data
        assertEquals(1, jobs.size)
        assertEquals("Office Dev", jobs[0].title)
        assertEquals("Austin, TX", jobs[0].location)
        // "Austin" doesn't contain "Remote", so it should be ON_SITE
        assertEquals(RemoteType.ON_SITE, jobs[0].remoteType)
    }

    @Test
    fun `searchJobs falls back to cache on API error`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        val cachedJobs = listOf(
            com.bangersoul.aivance.core.common.model.JobListing(
                id = "cached-1",
                title = "Cached Lever Job",
                company = "Startup",
                description = "",
                url = "",
                sourceProvider = "lever"
            )
        )
        coEvery { jobCache.getJobs() } returns cachedJobs

        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val provider = LeverProvider(
            companyId = "startup",
            jobCache = jobCache,
            okHttpClient = client,
            baseRetrofit = retrofit,
            baseUrl = mockWebServer.url("/").toString()
        )
        provider.onInitialize()
        provider.onStart()

        val result = provider.searchJobs(JobSearchFilter(), JobSortOrder.RELEVANCE, 1)

        assertTrue("Expected Success from cache, got $result", result is Result.Success)
        assertEquals("Cached Lever Job", (result as Result.Success).data[0].title)
    }
}
