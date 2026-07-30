package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ImportResumeUseCaseTest {

    private lateinit var resumeRepository: ResumeRepository
    private lateinit var useCase: ImportResumeUseCase

    @Before
    fun setUp() {
        resumeRepository = mockk()
        useCase = ImportResumeUseCase(resumeRepository)
    }

    @Test
    fun `should import valid resume successfully`() = runTest {
        val request = ImportResumeRequest(
            fileName = "resume.pdf",
            fileUri = "content://documents/resume.pdf",
            rawText = "Experienced software engineer with 5 years in full-stack development...",
            isPrimary = true
        )

        coEvery { resumeRepository.insertResume(any()) } returns Result.Success(1L)

        val result = useCase(request)

        assertTrue(result.isSuccess)
        val response = (result as Result.Success).data
        assertEquals(1L, response.resumeId)
        assertEquals("resume.pdf", response.resume.fileName)
        assertTrue(response.resume.isPrimary)
        coVerify { resumeRepository.insertResume(any()) }
    }

    @Test
    fun `should fail when file name is blank`() = runTest {
        val request = ImportResumeRequest(
            fileName = "",
            fileUri = "content://documents/resume.pdf",
            rawText = "Experienced software engineer"
        )

        val result = useCase(request)

        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail when file URI is blank`() = runTest {
        val request = ImportResumeRequest(
            fileName = "resume.pdf",
            fileUri = "",
            rawText = "Experienced software engineer"
        )

        val result = useCase(request)

        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail when raw text is too short`() = runTest {
        val request = ImportResumeRequest(
            fileName = "resume.pdf",
            fileUri = "content://documents/resume.pdf",
            rawText = "Short"
        )

        val result = useCase(request)

        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail when raw text exceeds maximum length`() = runTest {
        val request = ImportResumeRequest(
            fileName = "resume.pdf",
            fileUri = "content://documents/resume.pdf",
            rawText = "A".repeat(500001)
        )

        val result = useCase(request)

        assertTrue(result.isFailure)
    }

    @Test
    fun `should handle repository failure`() = runTest {
        val request = ImportResumeRequest(
            fileName = "resume.pdf",
            fileUri = "content://documents/resume.pdf",
            rawText = "Experienced software engineer with 5 years of experience..."
        )

        coEvery { resumeRepository.insertResume(any()) } returns Result.Failure(
            com.bangersoul.aivance.core.common.result.DatabaseError("DB error")
        )

        val result = useCase(request)

        assertTrue(result.isFailure)
    }
}
