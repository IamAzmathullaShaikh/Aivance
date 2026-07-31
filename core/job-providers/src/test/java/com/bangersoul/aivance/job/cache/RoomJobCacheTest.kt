package com.bangersoul.aivance.job.cache

import com.bangersoul.aivance.core.common.enums.EmploymentType
import com.bangersoul.aivance.core.common.enums.ExperienceLevel
import com.bangersoul.aivance.core.common.enums.RemoteType
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

    private fun companyEntity(id: Long, name: String, logoUrl: String? = null) = CompanyEntity(
        id = id,
        name = name,
        domain = null,
        logoUrl = logoUrl,
        website = null,
        industry = null,
        headquarters = null,
        socialLinks = emptyMap()
    )

    private fun jobEntity(
        id: Long,
        companyId: Long,
        title: String,
        type: String = "FULL_TIME",
        location: String? = "Remote",
        salaryMin: Double? = null,
        salaryMax: Double? = null,
        sourceProviderId: String = "DATABASE",
        postedDate: Long = System.currentTimeMillis()
    ) = JobEntity(
        id = id,
        companyId = companyId,
        title = title,
        location = location,
        type = type,
        remoteType = RemoteType.ON_SITE.name,
        experienceLevel = ExperienceLevel.NOT_SPECIFIED.name,
        salaryMin = salaryMin,
        salaryMax = salaryMax,
        currency = "USD",
        description = "Build Android apps.",
        descriptionHtml = null,
        url = "https://example.com/jobs/$id",
        sourceProviderId = sourceProviderId,
        postedDate = postedDate
    )

    @Test
    fun `getJobs maps entities to domain`() = runTest {
        val companyEntity = companyEntity(1L, "TechCorp", "https://techcorp.com/logo.png")
        val jobEntity = jobEntity(
            id = 1L,
            companyId = 1L,
            title = "Android Developer",
            salaryMin = 100000.0,
            salaryMax = 130000.0,
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
        assertEquals(100000.0, result[0].salaryMin!!, 0.001)
        assertEquals(130000.0, result[0].salaryMax!!, 0.001)
        assertEquals("Build Android apps.", result[0].description)
        assertEquals(1750000000000L, result[0].postedDate)
        assertEquals("DATABASE", result[0].sourceProvider)
        assertEquals(EmploymentType.FULL_TIME, result[0].employmentType)
    }

    @Test
    fun `getJobs handles unknown employment type`() = runTest {
        val companyEntity = companyEntity(1L, "Co")
        val jobEntity = jobEntity(
            id = 2L,
            companyId = 1L,
            title = "Unknown Type Job",
            type = "UNKNOWN_TYPE",
            location = ""
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
        val existingCompany = companyEntity(5L, "ExistingCorp")
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
