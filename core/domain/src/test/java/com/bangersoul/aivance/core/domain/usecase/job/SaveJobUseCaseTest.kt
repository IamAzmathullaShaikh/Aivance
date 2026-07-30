package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SaveJobUseCaseTest {

    private lateinit var jobTrackerRepository: JobTrackerRepository
    private lateinit var useCase: SaveJobUseCase

    @Before
    fun setUp() {
        jobTrackerRepository = mockk()
        useCase = SaveJobUseCase(jobTrackerRepository)
    }

    @Test
    fun `should save job successfully`() = runTest {
        val job = JobListing(id = "1", title = "Android Developer", company = "Tech Corp", description = "Job description", url = "https://example.com/job", sourceProvider = "test")
        coEvery { jobTrackerRepository.insertApplication(any()) } returns Result.Success(1L)

        val result = useCase(SaveJobRequest(jobListing = job))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank title`() = runTest {
        val job = JobListing(id = "1", title = "", company = "Tech Corp", description = "desc", url = "https://example.com", sourceProvider = "test")
        val result = useCase(SaveJobRequest(jobListing = job))
        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail for blank company`() = runTest {
        val job = JobListing(id = "1", title = "Android Dev", company = "", description = "desc", url = "https://example.com", sourceProvider = "test")
        val result = useCase(SaveJobRequest(jobListing = job))
        assertTrue(result.isFailure)
    }
}
