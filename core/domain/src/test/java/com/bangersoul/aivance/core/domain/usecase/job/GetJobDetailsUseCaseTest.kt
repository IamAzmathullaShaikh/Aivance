package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.JobRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetJobDetailsUseCaseTest {

    private lateinit var jobRepository: JobRepository
    private lateinit var useCase: GetJobDetailsUseCase

    @Before
    fun setUp() {
        jobRepository = mockk()
        useCase = GetJobDetailsUseCase(jobRepository)
    }

    @Test
    fun `should get job details successfully`() = runTest {
        val job = JobListing(id = "1", title = "Android Dev", company = "Tech Corp", description = "desc", url = "https://example.com", sourceProvider = "test")
        coEvery { jobRepository.getJobById("1") } returns flowOf(Result.Success(job))

        val result = useCase("1")
        assertTrue(result.isSuccess)
        assertEquals("Android Dev", (result as Result.Success).data.title)
    }

    @Test
    fun `should fail for blank job ID`() = runTest {
        val result = useCase("")
        assertTrue(result.isFailure)
    }
}
