package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportResumeUseCaseTest {

    private lateinit var resumeRepository: ResumeRepository
    private lateinit var useCase: ExportResumeUseCase

    @Before
    fun setUp() {
        resumeRepository = mockk()
        useCase = ExportResumeUseCase(resumeRepository)
    }

    @Test
    fun `should export resume as text`() = runTest {
        val resume = Resume(id = 1L, fileName = "resume.pdf", fileUri = "content://", rawText = "Experience:\n- Kotlin\n- Android")
        coEvery { resumeRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))

        val result = useCase(ExportResumeRequest(resumeId = 1L, format = ExportFormat.TXT))

        assertTrue(result.isSuccess)
        val text = (result as Result.Success).data
        assertTrue(text.contains("resume.pdf"))
        assertTrue(text.contains("Kotlin"))
    }

    @Test
    fun `should export resume as markdown`() = runTest {
        val resume = Resume(id = 1L, fileName = "resume.pdf", fileUri = "content://", rawText = "Experience:\n- Kotlin")
        coEvery { resumeRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))

        val result = useCase(ExportResumeRequest(resumeId = 1L, format = ExportFormat.MARKDOWN))

        assertTrue(result.isSuccess)
        val text = (result as Result.Success).data
        assertTrue(text.startsWith("#"))
    }

    @Test
    fun `should export resume as JSON`() = runTest {
        val resume = Resume(id = 1L, fileName = "resume.pdf", fileUri = "content://", rawText = "Kotlin experience")
        coEvery { resumeRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))

        val result = useCase(ExportResumeRequest(resumeId = 1L, format = ExportFormat.JSON))

        assertTrue(result.isSuccess)
        val text = (result as Result.Success).data
        assertTrue(text.contains("\"fileName\""))
    }

    @Test
    fun `should fail for invalid resume ID`() = runTest {
        val result = useCase(ExportResumeRequest(resumeId = 0L))
        assertTrue(result.isFailure)
    }
}
