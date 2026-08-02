package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.data.job.JobFilterMatcher
import com.bangersoul.aivance.core.data.job.JobNormalizer
import com.bangersoul.aivance.core.database.dao.CompanyDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.model.CompanyEntity
import com.bangersoul.aivance.core.database.model.JobEntity
import com.bangersoul.aivance.core.database.model.JobWithDetails
import com.bangersoul.aivance.sdk.infrastructure.ProviderRegistry
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class JobRepositoryImplTest {

    private lateinit var repository: JobRepositoryImpl
    private val jobDao: JobDao = mockk()
    private val companyDao: CompanyDao = mockk()
    private val providerRegistry: ProviderRegistry = mockk()
    private val normalizer = JobNormalizer()
    private val filterMatcher = JobFilterMatcher()

    @Before
    fun setUp() {
        repository = JobRepositoryImpl(jobDao, companyDao, providerRegistry, normalizer, filterMatcher)
    }

    @Test
    fun `searchJobs returns empty success when no providers available`() = runTest {
        every { providerRegistry.getAllProviders() } returns emptyList()

        val result = repository.searchJobs(JobSearchFilter(query = "engineer"), JobSortOrder.RELEVANCE)

        assertTrue(result is Result.Success)
        assertEquals(emptyList<JobListing>(), (result as Result.Success).data)
    }

    @Test
    fun `getJobById returns success when job exists`() = runTest {
        val jobWithDetails = JobWithDetails(
            job = JobEntity(
                id = 1L,
                companyId = 1L,
                title = "Engineer",
                location = "Remote",
                type = "FULL_TIME",
                remoteType = "REMOTE",
                experienceLevel = "SENIOR",
                salaryMin = 100.0,
                salaryMax = 200.0,
                currency = "USD",
                description = "desc",
                descriptionHtml = null,
                url = "url",
                sourceProviderId = "provider",
                postedDate = System.currentTimeMillis()
            ),
            company = CompanyEntity(
                id = 1L,
                name = "Tech",
                domain = null,
                logoUrl = null,
                website = null,
                industry = null,
                headquarters = null
            )
        )
        coEvery { jobDao.getJobWithDetailsById(1L) } returns jobWithDetails

        val result = repository.getJobById("1")

        assertTrue(result is Result.Success)
        assertEquals("Engineer", (result as Result.Success).data.title)
        assertEquals("Tech", (result as Result.Success).data.company)
    }

    @Test
    fun `getJobById returns failure when job not found`() = runTest {
        // runCatchingCore wraps every Throwable into Result.Failure.
        coEvery { jobDao.getJobWithDetailsById(1L) } returns null
        every { providerRegistry.getAllProviders() } returns emptyList()

        val result = repository.getJobById("1")

        assertTrue(result.isFailure)
        assertEquals("Job not found: 1", (result as Result.Failure).error.message)
    }
}
