package com.bangersoul.aivance.job.cache

import com.bangersoul.aivance.core.common.enums.EmploymentType
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.database.dao.CompanyDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.model.CompanyEntity
import com.bangersoul.aivance.core.database.model.JobEntity
import com.bangersoul.aivance.core.database.model.JobWithDetails
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RoomJobCacheTest {

    private lateinit var jobDao: JobDao
    private lateinit var companyDao: CompanyDao
    private lateinit var cache: RoomJobCache

    @Before
    fun setUp() {
        jobDao = mockk(relaxed = true)
        companyDao = mockk(relaxed = true)
        cache = RoomJobCache(jobDao, companyDao)
    }

    @Test
    fun `getJobs maps entities to domain`() = runTest {
        val companyEntity = CompanyEntity(
            id = 1L,
            name = "TechCorp",
            logoUrl = "https://techcorp.com/logo.png",
            website = null,
            industry = null
        )
        val jobEntity = JobEntity(
            id = 1L,
            companyId = 1L,
            title = "Android Developer",
            location = "Remote",
            type = "FULL_TIME",
            salary = "$100k - $130k",
            description = "Build Android apps.",
            postedDate = 1750000000000L
        )
        val jobWithDetails = JobWithDetails(job = jobEntity, company = companyEntity)
        
        coEvery { jobDao.getJobsWithDetails() } returns flowOf(listOf(jobWithDetails))

        val result = cache.getJobs()

        assertEquals(1, result.size)
        assertEquals("Android Developer", result[0].title)
        assertEquals("TechCorp", result[0].company)
        assertEquals("https://techcorp.com/logo.png", result[0].companyLogoUrl)
        assertEquals("Remote", result[0].location)
        assertEquals("$100k - $130k", result[0].salaryRange)
        assertEquals("Build Android apps.", result[0].description)
        assertEquals(1750000000000L, result[0].postedDate)
        assertEquals("DATABASE", result[0].sourceProvider)
        assertEquals(EmploymentType.FULL_TIME, result[0].employmentType)
    }

    @Test
    fun `getJobs handles unknown employment type`() = runTest {
        val companyEntity = CompanyEntity(id = 1L, name = "Co", logoUrl = null, website = null, industry = null)
        val jobEntity = JobEntity(
            id = 2L,
            companyId = 1L,
            title = "Unknown Type Job",
            location = "",
            type = "UNKNOWN_TYPE",
            salary = null,
            description = "",
            postedDate = System.currentTimeMillis()
        )
        val jobWithDetails = JobWithDetails(job = jobEntity, company = companyEntity)
        
        coEvery { jobDao.getJobsWithDetails() } returns flowOf(listOf(jobWithDetails))

        val result = cache.getJobs()

        assertEquals(1, result.size)
        assertEquals(EmploymentType.FULL_TIME, result[0].employmentType)
    }

    @Test
    fun `saveJobs inserts company and job for each listing`() = runTest {
        coEvery { companyDao.getCompanyByName("NewCo") } returns null
        coEvery { companyDao.insertCompany(any()) } returns 1L

        val jobs = listOf(
            JobListing(
                id = "ext-1",
                title = "Engineer",
                company = "NewCo",
                companyLogoUrl = "https://newco.com/logo.png",
                salaryRange = "$100k",
                description = "Do stuff",
                url = "https://newco.com/jobs/1",
                sourceProvider = "test",
                employmentType = EmploymentType.FULL_TIME,
                location = "NYC",
                postedDate = System.currentTimeMillis()
            )
        )

        cache.saveJobs(jobs)

        coVerify { companyDao.getCompanyByName("NewCo") }
        coVerify { companyDao.insertCompany(any()) }
        coVerify { jobDao.insertJob(any()) }
    }

    @Test
    fun `saveJobs reuses existing company`() = runTest {
        val existingCompany = CompanyEntity(id = 5L, name = "ExistingCorp", logoUrl = null, website = null, industry = null)
        coEvery { companyDao.getCompanyByName("ExistingCorp") } returns existingCompany

        val jobs = listOf(
            JobListing(
                id = "ext-2",
                title = "Senior Engineer",
                company = "ExistingCorp",
                description = "",
                url = "",
                sourceProvider = "test",
                employmentType = EmploymentType.FULL_TIME
            )
        )

        cache.saveJobs(jobs)

        coVerify(exactly = 0) { companyDao.insertCompany(any()) }
        coVerify { jobDao.insertJob(
            match { it.companyId == 5L }
        ) }
    }

    @Test
    fun `clear delegates to jobDao`() = runTest {
        cache.clear()
        coVerify { jobDao.deleteAllJobs() }
    }
}
