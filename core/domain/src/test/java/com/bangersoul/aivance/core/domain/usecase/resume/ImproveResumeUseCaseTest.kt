package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeAnalysis
import com.bangersoul.aivance.core.common.result.Result
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
    private lateinit var useCase: ImproveResumeUseCase

    @Before
    fun setUp() {
        resumeRepository = mockk()
        useCase = ImproveResumeUseCase(resumeRepository)
    }

    @Test
    fun `should improve resume with analysis`() = runTest {
        val resume = Resume(id = 1L, fileName = "resume.pdf", fileUri = "content://", rawText = "Old text", sections = emptyList())
        val analysis = ResumeAnalysis(overallScore = 60, missingKeywords = listOf("Kotlin", "Compose"), matchSummary = "Needs improvement")

        coEvery { resumeRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))
        coEvery { resumeRepository.analyzeResume(1L, any()) } returns Result.Success(analysis)
        coEvery { resumeRepository.updateResume(any()) } returns Result.Success(Unit)

        val result = useCase(ImproveResumeRequest(resumeId = 1L, jobDescription = "Kotlin developer position"))

        assertTrue(result.isSuccess)
        val response = (result as Result.Success).data
        assertTrue(response.changes.any { it.contains("Kotlin") })
        coVerify { resumeRepository.updateResume(any()) }
    }

    @Test
    fun `should fail for invalid resume ID`() = runTest {
        val result = useCase(ImproveResumeRequest(resumeId = 0L))
        assertTrue(result.isFailure)
    }

    @Test
    fun `should handle resume not found`() = runTest {
        coEvery { resumeRepository.getResumeById(1L) } returns flowOf(
            Result.Failure(com.bangersoul.aivance.core.common.result.DatabaseError("Not found"))
        )

        val result = useCase(ImproveResumeRequest(resumeId = 1L))
        assertTrue(result.isFailure)
    }
}
