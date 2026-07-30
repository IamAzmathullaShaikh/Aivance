package com.bangersoul.aivance.core.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.SearchFilter
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.data.source.JobLocalDataSource
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.model.JobWithDetails
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class JobRepositoryImplTest {

    private lateinit var repository: JobRepositoryImpl
    private val localDataSource: JobLocalDataSource = mockk()
    private val jobDao: JobDao = mockk()

    @Before
    fun setUp() {
        repository = JobRepositoryImpl(localDataSource, jobDao)
    }

    @Test
    fun `searchJobs returns PagingData flow`() = runTest {
        val pagingSource = object : PagingSource<Int, JobWithDetails>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, JobWithDetails> {
                return LoadResult.Page(emptyList(), null, null)
            }
            override fun getRefreshKey(state: PagingState<Int, JobWithDetails>): Int? = null
        }
        every { jobDao.getJobsPagingSource() } returns pagingSource

        repository.searchJobs("query", SearchFilter()).test {
            val pagingData = awaitItem()
            assertNotNull(pagingData)
            // PagingData is hard to inspect directly, but we verified the stream emitted
        }
    }

    @Test
    fun `getJobById returns success when job exists`() = runTest {
        val jobId = "job1"
        val jobs = listOf(
            JobListing(
                id = jobId,
                title = "Engineer",
                company = "Tech",
                description = "desc",
                url = "url",
                sourceProvider = "provider"
            )
        )
        every { localDataSource.getJobs() } returns flowOf(jobs)

        repository.getJobById(jobId).test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(jobs.first(), (result as Result.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun `getJobById returns failure when job not found`() = runTest {
        val jobId = "job1"
        every { localDataSource.getJobs() } returns flowOf(emptyList())

        repository.getJobById(jobId).test {
            val result = awaitItem()
            assertTrue(result is Result.Failure)
            assertEquals("Job not found", (result as Result.Failure).error.message)
            awaitComplete()
        }
    }
}
