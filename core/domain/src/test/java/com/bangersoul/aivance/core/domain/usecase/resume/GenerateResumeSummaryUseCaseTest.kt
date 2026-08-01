package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.Resume
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

class GenerateResumeSummaryUseCaseTest {

    private lateinit var resumeRepository: ResumeRepository
    private lateinit var useCase: GenerateResumeSummaryUseCase

    @Before
    fun setUp() {
        resumeRepository = mockk()
        useCase = GenerateResumeSummaryUseCase(resumeRepository)
    }

    @Test
    fun `should generate summary successfully`() = runTest {
        val resumeText = buildString {
            appendLine("SUMMARY")
            appendLine("Experienced developer with 5 years in Android")
            appendLine("SKILLS")
            appendLine("Kotlin, Jetpack Compose")
            appendLine("EXPERIENCE")
            appendLine("Senior Dev at Google")
        }
        val resume = Resume(id = 1L, name = "resume.pdf", rawText = resumeText)
        coEvery { resumeRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))

        val result = useCase(ResumeSummaryRequest(resumeId = 1L, maxLength = 500))

        assertTrue(result.isSuccess)
        val summary = (result as Result.Success).data
        assertTrue(summary.isNotEmpty())
        assertTrue(summary.contains("Skills: Kotlin, Jetpack Compose"))
        assertTrue(summary.length <= 503)
    }

    @Test
    fun `should fail for invalid resume ID`() = runTest {
        val result = useCase(ResumeSummaryRequest(resumeId = 0L))

        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail for invalid max length`() = runTest {
        val result = useCase(ResumeSummaryRequest(resumeId = 1L, maxLength = 0))

        assertTrue(result.isFailure)
    }

    @Test
    fun `should truncate summary to max length`() = runTest {
        // Skills section extraction caps at 200 chars, so the generated summary
        // exceeds the 100-char budget and must be truncated to exactly 100.
        val resumeText = buildString {
            appendLine("SKILLS")
            append("A".repeat(300))
        }
        val resume = Resume(id = 1L, name = "resume.pdf", rawText = resumeText)
        coEvery { resumeRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))

        val result = useCase(ResumeSummaryRequest(resumeId = 1L, maxLength = 100))

        assertTrue(result.isSuccess)
        val summary = (result as Result.Success).data
        assertEquals(100, summary.length)
        assertTrue(summary.endsWith("..."))
    }
}
