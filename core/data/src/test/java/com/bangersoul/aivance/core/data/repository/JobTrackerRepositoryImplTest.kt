package com.bangersoul.aivance.core.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.JobApplication
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.data.source.JobLocalDataSource
import com.bangersoul.aivance.core.database.dao.TrackerDao
import com.bangersoul.aivance.core.database.model.JobApplicationEntity
import com.bangersoul.aivance.core.database.model.JobApplicationWithDetails
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class JobTrackerRepositoryImplTest {

    private lateinit var repository: JobTrackerRepositoryImpl
    private val localDataSource: JobLocalDataSource = mockk()
    private val trackerDao: TrackerDao = mockk()

    @Before
    fun setUp() {
        repository = JobTrackerRepositoryImpl(localDataSource, trackerDao)
    }

    @Test
    fun `getApplications returns PagingData flow`() = runTest {
        val pagingSource = object : PagingSource<Int, JobApplicationWithDetails>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, JobApplicationWithDetails> {
                return LoadResult.Page(emptyList(), null, null)
            }
            override fun getRefreshKey(state: PagingState<Int, JobApplicationWithDetails>): Int? = null
        }
        every { trackerDao.getApplicationsPagingSource() } returns pagingSource

        repository.getApplications().test {
            val pagingData = awaitItem()
            assertNotNull(pagingData)
        }
    }

    @Test
    fun `getApplicationById returns success when application exists`() = runTest {
        val id = 1L
        val apps = listOf(JobApplication(id = id, company = "Google", role = "Dev"))
        every { localDataSource.getApplications() } returns flowOf(apps)

        repository.getApplicationById(id).test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(apps.first(), (result as Result.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun `insertApplication calls localDataSource`() = runTest {
        val app = JobApplication(id = 1, company = "Google", role = "Dev")
        coEvery { localDataSource.saveApplication(app, app.id) } returns 1L

        val result = repository.insertApplication(app)

        assertTrue(result is Result.Success)
        assertEquals(1L, (result as Result.Success).data)
    }

    @Test
    fun `updateApplication calls trackerDao update`() = runTest {
        val app = JobApplication(id = 1, company = "Google", role = "Dev")
        val entity = JobApplicationEntity(id = 1, jobId = 10, status = "SAVED", dateApplied = 0, salaryRange = "", notes = "", lastModified = 0)
        coEvery { trackerDao.getApplicationById(1L) } returns entity
        coEvery { trackerDao.updateApplication(any()) } returns Unit

        val result = repository.updateApplication(app)

        assertTrue(result is Result.Success)
        coVerify { trackerDao.updateApplication(match { it.id == 1L }) }
    }

    @Test
    fun `deleteApplication calls trackerDao delete`() = runTest {
        val id = 1L
        val entity = JobApplicationEntity(id = id, jobId = 10, status = "SAVED", dateApplied = 0, salaryRange = "", notes = "", lastModified = 0)
        coEvery { trackerDao.getApplicationById(id) } returns entity
        coEvery { trackerDao.deleteApplication(entity) } returns Unit

        val result = repository.deleteApplication(id)

        assertTrue(result is Result.Success)
        coVerify { trackerDao.deleteApplication(entity) }
    }
}
