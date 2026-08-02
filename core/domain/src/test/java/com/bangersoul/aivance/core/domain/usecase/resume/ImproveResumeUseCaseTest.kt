package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ImproveResumeUseCaseTest {

    private lateinit var resumeRepository: ResumeRepository
    private lateinit var aiRepository: AiRepository
    private lateinit var useCase: ImproveResumeUseCase

    @Before
    fun setUp() {
        resumeRepository = mockk()
        aiRepository = mockk()
        useCase = ImproveResumeUseCase(resumeRepository, aiRepository)
    }

    @Test
    fun `should create improved version with new id`() = runTest {
        val original = ResumeVersion(id = 2L, resumeId = 1L, versionName = "v1")
        coEvery { resumeRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(original)))
        coEvery { resumeRepository.saveVersion(any()) } returns Result.Success(99L)
        coEvery { aiRepository.analyzeText(any(), any()) } returns Result.Success("Improved content")

        val result = useCase(ImproveResumeRequest(resumeId = 1L, versionId = 2L))

        assertTrue(result.isSuccess)
        val improved = (result as Result.Success).data
        assertEquals(99L, improved.id)
        assertEquals("v1 (Improved)", improved.versionName)
        coVerify { resumeRepository.saveVersion(any()) }
    }

    @Test
    fun `should fail when version is not found`() = runTest {
        coEvery { resumeRepository.getVersions(1L) } returns flowOf(Result.Success(emptyList()))

        val result = useCase(ImproveResumeRequest(resumeId = 1L, versionId = 99L))

        assertTrue(result.isFailure)
    }

    @Test
    fun `should handle repository failure when fetching versions`() = runTest {
        coEvery { resumeRepository.getVersions(1L) } returns flowOf(
            Result.Failure(DomainError("Resume not found"))
        )

        val result = useCase(ImproveResumeRequest(resumeId = 1L, versionId = 2L))

        assertTrue(result.isFailure)
    }
}
