package com.bangersoul.aivance.job.remotive

import com.bangersoul.aivance.core.common.enums.JobSortOrder
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

class RemotiveProviderTest {

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
            {
                "job-count": 2,
                "jobs": [
                    {
                        "id": 101,
                        "url": "https://remotive.com/job/101",
                        "title": "DevOps Engineer",
                        "company_name": "CloudOps",
                        "company_logo": "https://cloudops.com/logo.png",
                        "category": "Engineering",
                        "tags": ["aws", "terraform"],
                        "job_type": "full_time",
                        "publication_date": "2026-07-12T10:00:00",
                        "candidate_required_location": "Worldwide",
                        "salary": "$150k",
                        "description": "Manage cloud infra."
                    },
                    {
                        "id": 102,
                        "url": "https://remotive.com/job/102",
                        "title": "Data Scientist",
                        "company_name": "DataLab",
                        "category": "Data",
                        "job_type": "contract",
                        "publication_date": "2026-07-11T08:30:00",
                        "candidate_required_location": "Remote",
                        "description": "ML models."
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

        val provider = RemotiveProvider(jobCache, client, retrofit)
        provider.onInitialize()
        provider.onStart()

        val result = provider.searchJobs(JobSearchFilter(), JobSortOrder.RELEVANCE, 1)

        assertTrue("Expected Success, got $result", result is Result.Success)
        val jobs = (result as Result.Success).data
        assertEquals(2, jobs.size)
        assertEquals("DevOps Engineer", jobs[0].title)
        assertEquals("CloudOps", jobs[0].company)
        assertEquals("https://cloudops.com/logo.png", jobs[0].companyLogoUrl)
        assertEquals(com.bangersoul.aivance.core.common.enums.EmploymentType.FULL_TIME, jobs[0].employmentType)
        assertTrue(jobs[0].isRemote)
    }

    @Test
    fun `searchJobs returns failure on API error`() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Error"))
        coEvery { jobCache.getJobs() } returns emptyList()

        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        val provider = RemotiveProvider(jobCache, client, retrofit)
        provider.onInitialize()
        provider.onStart()

        val result = provider.searchJobs(JobSearchFilter(), JobSortOrder.RELEVANCE, 1)

        assertTrue("Expected Failure, got $result", result is Result.Failure)
    }
}
