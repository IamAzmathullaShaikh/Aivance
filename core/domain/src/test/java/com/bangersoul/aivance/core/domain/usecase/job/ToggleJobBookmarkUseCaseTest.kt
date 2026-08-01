package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.JobRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Replaces the pre-Phase-12 BookmarkJobUseCase/SaveJobUseCase/RemoveSavedJobUseCase tests.
 * Job bookmarking is now a single toggle operation on [JobRepository].
 */
class ToggleJobBookmarkUseCaseTest {

    private lateinit var jobRepository: JobRepository
    private lateinit var useCase: ToggleJobBookmarkUseCase

    @Before
    fun setUp() {
        jobRepository = mockk()
        useCase = ToggleJobBookmarkUseCase(jobRepository)
    }

    @Test
    fun `should toggle bookmark to saved`() = runTest {
        coEvery { jobRepository.toggleBookmark("1") } returns Result.Success(true)

        val result = useCase("1")

        assertTrue(result.isSuccess)
        assertEquals(true, (result as Result.Success).data)
    }

    @Test
    fun `should toggle bookmark to unsaved`() = runTest {
        coEvery { jobRepository.toggleBookmark("1") } returns Result.Success(false)

        val result = useCase("1")

        assertTrue(result.isSuccess)
        assertEquals(false, (result as Result.Success).data)
    }

    @Test
    fun `should propagate repository failure`() = runTest {
        coEvery { jobRepository.toggleBookmark("1") } returns Result.Failure(DomainError("Job not found"))

        val result = useCase("1")

        assertTrue(result.isFailure)
    }
}
