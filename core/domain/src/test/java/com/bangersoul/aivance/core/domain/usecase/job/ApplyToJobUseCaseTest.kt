package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApplyToJobUseCaseTest {

    private lateinit var jobTrackerRepository: JobTrackerRepository
    private lateinit var useCase: ApplyToJobUseCase

    @Before
    fun setUp() {
        jobTrackerRepository = mockk()
        useCase = ApplyToJobUseCase(jobTrackerRepository)
    }

    @Test
    fun `should apply to job successfully`() = runTest {
        coEvery { jobTrackerRepository.insertApplication(any()) } returns Result.Success(1L)

        val result = useCase(ApplyToJobRequest(company = "Tech Corp", role = "Android Developer"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank company`() = runTest {
        val result = useCase(ApplyToJobRequest(company = "", role = "Android Dev"))
        assertTrue(result.isFailure)
    }
}
