package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BookmarkJobUseCaseTest {

    private lateinit var jobTrackerRepository: JobTrackerRepository
    private lateinit var useCase: BookmarkJobUseCase

    @Before
    fun setUp() {
        jobTrackerRepository = mockk()
        useCase = BookmarkJobUseCase(jobTrackerRepository)
    }

    @Test
    fun `should bookmark job successfully`() = runTest {
        coEvery { jobTrackerRepository.insertApplication(any()) } returns Result.Success(1L)

        val result = useCase(BookmarkJobRequest(company = "Tech Corp", role = "Android Developer"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank company`() = runTest {
        val result = useCase(BookmarkJobRequest(company = "", role = "Android Dev"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail for blank role`() = runTest {
        val result = useCase(BookmarkJobRequest(company = "Tech Corp", role = ""))
        assertTrue(result.isFailure)
    }
}
