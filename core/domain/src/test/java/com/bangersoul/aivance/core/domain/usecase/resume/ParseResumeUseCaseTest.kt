package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.Resume
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

class ParseResumeUseCaseTest {

    private lateinit var resumeRepository: ResumeRepository
    private lateinit var useCase: ParseResumeUseCase

    @Before
    fun setUp() {
        resumeRepository = mockk()
        useCase = ParseResumeUseCase(resumeRepository)
    }

    @Test
    fun `should parse resume sections successfully`() = runTest {
        val resumeText = buildString {
            appendLine("SUMMARY")
            appendLine("Experienced software engineer")
            appendLine()
            appendLine("EXPERIENCE")
            appendLine("Senior Developer at Tech Corp (2020-2024)")
            appendLine("- Led team of 5 engineers")
            appendLine()
            appendLine("EDUCATION")
            appendLine("B.S. Computer Science, 2018")
            appendLine()
            appendLine("SKILLS")
            appendLine("Kotlin, Java, Android")
        }

        val resume = Resume(
            id = 1L,
            fileName = "resume.pdf",
            fileUri = "content://resume.pdf",
            rawText = resumeText
        )

        coEvery { resumeRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))
        coEvery { resumeRepository.updateSections(any(), any()) } returns Result.Success(Unit)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val response = (result as Result.Success).data
        assertTrue(response.sections.isNotEmpty())
        assertEquals("Summary", response.sections[0].title)
        coVerify { resumeRepository.updateSections(any(), any()) }
    }

    @Test
    fun `should fail for invalid resume ID`() = runTest {
        val result = useCase(0L)

        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail for negative resume ID`() = runTest {
        val result = useCase(-1L)

        assertTrue(result.isFailure)
    }

    @Test
    fun `should return empty sections for text without headers`() = runTest {
        val resumeText = "Just a plain text without any section headers..."
        val resume = Resume(
            id = 1L,
            fileName = "resume.txt",
            fileUri = "content://resume.txt",
            rawText = resumeText
        )

        coEvery { resumeRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))
        coEvery { resumeRepository.updateSections(any(), any()) } returns Result.Success(Unit)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        val response = (result as Result.Success).data
        assertEquals(0, response.sections.size)
    }

    @Test
    fun `should handle repository error when fetching resume`() = runTest {
        coEvery { resumeRepository.getResumeById(1L) } returns flowOf(
            Result.Failure(com.bangersoul.aivance.core.common.result.DatabaseError("Not found"))
        )

        val result = useCase(1L)

        assertTrue(result.isFailure)
    }
}
