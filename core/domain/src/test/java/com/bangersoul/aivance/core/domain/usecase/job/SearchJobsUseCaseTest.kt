package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.JobRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchJobsUseCaseTest {

    private lateinit var jobRepository: JobRepository
    private lateinit var useCase: SearchJobsUseCase

    @Before
    fun setUp() {
        jobRepository = mockk()
        useCase = SearchJobsUseCase(jobRepository)
    }

    @Test
    fun `should search jobs successfully`() = runTest {
        val jobs = listOf(
            JobListing(
                id = "1",
                title = "Android Developer",
                company = "Tech Corp",
                description = "desc",
                url = "https://example.com",
                sourceProvider = "test"
            )
        )
        coEvery { jobRepository.searchJobs(any(), any()) } returns Result.Success(jobs)

        val result = useCase(
            SearchJobsRequest(
                filter = JobSearchFilter(query = "Android Developer"),
                sortOrder = JobSortOrder.DATE_DESC
            )
        )

        assertTrue(result.isSuccess)
        assertEquals(1, (result as Result.Success).data.size)
    }

    @Test
    fun `should propagate repository failure`() = runTest {
        coEvery { jobRepository.searchJobs(any(), any()) } returns Result.Failure(
            com.bangersoul.aivance.core.common.result.DomainError("No providers configured")
        )

        val result = useCase(SearchJobsRequest())

        assertTrue(result.isFailure)
    }
}
