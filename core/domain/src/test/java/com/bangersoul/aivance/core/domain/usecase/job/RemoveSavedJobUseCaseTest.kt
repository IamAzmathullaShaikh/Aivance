package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoveSavedJobUseCaseTest {

    private lateinit var jobTrackerRepository: JobTrackerRepository
    private lateinit var useCase: RemoveSavedJobUseCase

    @Before
    fun setUp() {
        jobTrackerRepository = mockk()
        useCase = RemoveSavedJobUseCase(jobTrackerRepository)
    }

    @Test
    fun `should remove saved job successfully`() = runTest {
        coEvery { jobTrackerRepository.deleteApplication(1L) } returns Result.Success(Unit)

        val result = useCase(1L)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for invalid ID`() = runTest {
        val result = useCase(0L)
        assertTrue(result.isFailure)
    }
}
