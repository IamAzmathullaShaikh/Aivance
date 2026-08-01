package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeAnalysis
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateATSScoreUseCaseTest {

    private lateinit var resumeRepository: ResumeRepository
    private lateinit var useCase: CalculateATSScoreUseCase

    @Before
    fun setUp() {
        resumeRepository = mockk()
        useCase = CalculateATSScoreUseCase(resumeRepository)
    }

    @Test
    fun `should calculate ATS score successfully`() = runTest {
        val resumeText = buildString {
            appendLine("test@email.com")
            appendLine("+1234567890")
            appendLine("Experienced Kotlin developer")
            appendLine("- Led Android team")
        }
        val resume = Resume(id = 1L, name = "resume.pdf", rawText = resumeText)
        val analysis = ResumeAnalysis(
            overallScore = 80,
            matchingKeywords = listOf("Kotlin"),
            missingKeywords = listOf("Compose"),
            matchSummary = "Good match"
        )

        coEvery { resumeRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))
        coEvery { resumeRepository.analyzeResume(1L, 2L, any()) } returns Result.Success(analysis)

        val result = useCase(
            AtsScoreRequest(resumeId = 1L, versionId = 2L, jobDescription = "Kotlin developer needed")
        )

        assertTrue(result.isSuccess)
        val response = (result as Result.Success).data
        assertEquals(80, response.atsResult.score)
        assertEquals("resume.pdf", response.atsResult.resumeName)
        assertTrue(response.atsResult.matchingKeywords.contains("Kotlin"))
        assertEquals(80, response.analysis.overallScore)
    }

    @Test
    fun `should fail for invalid resume ID`() = runTest {
        val result = useCase(
            AtsScoreRequest(resumeId = 0L, versionId = 2L, jobDescription = "Job description")
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail for blank job description`() = runTest {
        val result = useCase(
            AtsScoreRequest(resumeId = 1L, versionId = 2L, jobDescription = "")
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun `should clamp score between 0 and 100`() = runTest {
        val resume = Resume(id = 1L, name = "resume.pdf", rawText = "Test content")
        val analysis = ResumeAnalysis(overallScore = 150)

        coEvery { resumeRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))
        coEvery { resumeRepository.analyzeResume(1L, 2L, any()) } returns Result.Success(analysis)

        val result = useCase(
            AtsScoreRequest(resumeId = 1L, versionId = 2L, jobDescription = "Test job")
        )

        assertTrue(result.isSuccess)
        assertEquals(100, (result as Result.Success).data.atsResult.score)
    }
}
