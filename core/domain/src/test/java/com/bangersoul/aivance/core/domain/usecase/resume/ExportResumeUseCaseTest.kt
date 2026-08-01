package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.ResumeSection
import com.bangersoul.aivance.core.common.model.ResumeVersion
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

    private fun sampleVersion(): ResumeVersion = ResumeVersion(
        id = 5L,
        resumeId = 1L,
        versionName = "v1",
        sections = listOf(
            ResumeSection(
                sectionType = "EXPERIENCE",
                title = "Experience",
                content = "Kotlin developer with 5 years"
            )
        )
    )

    @Test
    fun `should export resume as text`() = runTest {
        coEvery { resumeRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(sampleVersion())))

        val result = useCase(ExportResumeRequest(resumeId = 1L, versionId = 5L, format = ExportFormat.TXT))

        assertTrue(result.isSuccess)
        val text = (result as Result.Success).data
        assertTrue(text.contains("v1"))
        assertTrue(text.contains("Kotlin developer with 5 years"))
    }

    @Test
    fun `should export resume as markdown`() = runTest {
        coEvery { resumeRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(sampleVersion())))

        val result = useCase(ExportResumeRequest(resumeId = 1L, versionId = 5L, format = ExportFormat.MARKDOWN))

        assertTrue(result.isSuccess)
        val text = (result as Result.Success).data
        assertTrue(text.startsWith("#"))
        assertTrue(text.contains("## Experience"))
    }

    @Test
    fun `should export resume as JSON`() = runTest {
        coEvery { resumeRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(sampleVersion())))

        val result = useCase(ExportResumeRequest(resumeId = 1L, versionId = 5L, format = ExportFormat.JSON))

        assertTrue(result.isSuccess)
        val text = (result as Result.Success).data
        assertTrue(text.contains("\"versionName\""))
        assertTrue(text.contains("\"title\""))
    }

    @Test
    fun `should fail when version is not found`() = runTest {
        coEvery { resumeRepository.getVersions(1L) } returns flowOf(Result.Success(emptyList()))

        val result = useCase(ExportResumeRequest(resumeId = 1L, versionId = 99L, format = ExportFormat.TXT))

        assertTrue(result.isFailure)
    }
}
