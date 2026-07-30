package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.database.dao.CompanyDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.dao.SearchDao
import com.bangersoul.aivance.core.database.dao.TrackerDao
import com.bangersoul.aivance.core.data.source.JobLocalDataSourceImpl
import com.bangersoul.aivance.core.database.model.CompanyEntity
import com.bangersoul.aivance.core.database.model.JobApplicationEntity
import com.bangersoul.aivance.core.database.model.JobApplicationWithDetails
import com.bangersoul.aivance.core.database.model.JobEntity
import com.bangersoul.aivance.core.database.model.JobWithDetails
import com.bangersoul.aivance.core.database.model.SavedSearchEntity
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Repository integration tests using MockK for backing DAOs.
 *
 * Tests verify the data layer mapping from Room entities to domain models,
 * covering the full flow: DAO → Entity → Mapper → Domain → Repository.
 */
class JobRepositoryIntegrationTest {

    private lateinit var mockJobDao: JobDao
    private lateinit var mockCompanyDao: CompanyDao
    private lateinit var mockSearchDao: SearchDao
    private lateinit var mockTrackerDao: TrackerDao
    private lateinit var localDataSource: JobLocalDataSourceImpl

    @Before
    fun setup() {
        mockJobDao = mockk()
        mockCompanyDao = mockk()
        mockSearchDao = mockk()
        mockTrackerDao = mockk()
        localDataSource = JobLocalDataSourceImpl(
            jobDao = mockJobDao,
            companyDao = mockCompanyDao,
            searchDao = mockSearchDao,
            trackerDao = mockTrackerDao
        )
    }

    @Test
    fun `getJobs maps entities to domain correctly`() = runBlocking {
        val companyEntity = CompanyEntity(
            id = 1, name = "Tech Corp", logoUrl = null, website = null, industry = "Technology"
        )
        val sampleEntities = listOf(
            JobEntity(
                id = 1, companyId = 1, title = "Android Engineer",
                location = "Mountain View", type = "FULL_TIME",
                salary = "$150k-$200k", description = "Build Android apps",
                postedDate = System.currentTimeMillis()
            ),
            JobEntity(
                id = 2, companyId = 1, title = "iOS Engineer",
                location = "Cupertino", type = "FULL_TIME",
                salary = "$160k-$210k", description = "Build iOS apps",
                postedDate = System.currentTimeMillis()
            )
        )

        every { mockJobDao.getJobsWithDetails() } returns flowOf(
            sampleEntities.map { entity ->
                JobWithDetails(
                    job = entity,
                    company = companyEntity
                )
            }
        )

        val jobs = localDataSource.getJobs().first()

        assertEquals(2, jobs.size)
        assertEquals("Android Engineer", jobs[0].title)
        assertEquals("Tech Corp", jobs[0].company)
        assertEquals("iOS Engineer", jobs[1].title)
    }

    @Test
    fun `saveJob inserts entity and returns id`() = runBlocking {
        val sampleJob = com.bangersoul.aivance.core.common.model.JobListing(
            id = "1", title = "Backend Engineer", company = "DataCo",
            location = "Remote", description = "Build APIs", url = "",
            sourceProvider = "REMOTE_OK"
        )

        coEvery { mockCompanyDao.insertCompany(any()) } returns 1L
        coEvery { mockJobDao.insertJob(any()) } returns 1L

        val companyId = localDataSource.saveCompany(
            com.bangersoul.aivance.core.common.model.Company(
                id = "1", name = "DataCo"
            )
        )
        val jobId = localDataSource.saveJob(sampleJob, companyId)

        assertTrue(jobId > 0)
    }

    @Test
    fun `getApplications maps tracker entities correctly`() = runBlocking {
        val companyEntity = CompanyEntity(
            id = 1, name = "Tech Corp", logoUrl = null, website = null, industry = "Technology"
        )
        val jobEntity = JobEntity(
            id = 1, companyId = 1, title = "Android Engineer",
            location = "Remote", type = "FULL_TIME",
            salary = "$150k", description = "Build apps",
            postedDate = System.currentTimeMillis()
        )
        val appEntity = JobApplicationEntity(
            id = 1, jobId = 1, status = "INTERVIEWING",
            dateApplied = System.currentTimeMillis(),
            salaryRange = "$150k-$200k", notes = "Phone screen scheduled",
            lastModified = System.currentTimeMillis()
        )

        every { mockTrackerDao.getApplications() } returns flowOf(
            listOf(
                JobApplicationWithDetails(
                    application = appEntity,
                    job = JobWithDetails(job = jobEntity, company = companyEntity)
                )
            )
        )

        val applications = localDataSource.getApplications().first()

        assertEquals(1, applications.size)
        assertEquals("INTERVIEWING", applications[0].status.name)
    }

    @Test
    fun `getSavedSearches maps search entities`() = runBlocking {
        val sampleEntities = listOf(
            SavedSearchEntity(
                query = "Android Developer",
                filters = mapOf("location" to "Remote", "isRemote" to "true"),
                dateCreated = java.time.Instant.now()
            )
        )

        every { mockSearchDao.getSavedSearches() } returns flowOf(sampleEntities)

        val searches = localDataSource.getSavedSearches().first()

        assertEquals(1, searches.size)
        assertEquals("Android Developer", searches[0].keywords)
        assertTrue(searches[0].isRemote)
    }

    @Test
    fun `getJobById returns null when not found`() = runBlocking {
        coEvery { mockJobDao.getJobById(any()) } returns null
        val job = localDataSource.getJobById(999L)
        assertEquals(null, job)
    }
}
