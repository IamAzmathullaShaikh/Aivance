package com.bangersoul.aivance.core.domain.usecase.coverletter

import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.model.CoverLetterSection
import com.bangersoul.aivance.core.common.model.CoverLetterVersion
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportCoverLetterUseCaseTest {

    private lateinit var coverLetterRepository: CoverLetterRepository
    private lateinit var useCase: ExportCoverLetterUseCase

    @Before
    fun setUp() {
        coverLetterRepository = mockk()
        useCase = ExportCoverLetterUseCase(coverLetterRepository)
    }

    private fun sampleLetter() = CoverLetter(
        id = 1L,
        resumeVersionId = 1L,
        jobId = 2L,
        recruiterId = null,
        company = "Tech Corp",
        role = "Engineer",
        versions = listOf(
            CoverLetterVersion(
                id = 10L,
                coverLetterId = 1L,
                versionName = "v1",
                writingStyle = "PROFESSIONAL",
                sections = listOf(
                    CoverLetterSection(
                        id = 100L,
                        versionId = 10L,
                        sectionType = "OPENING",
                        title = "Opening",
                        content = "Dear Hiring Manager..."
                    )
                )
            )
        )
    )

    @Test
    fun `should export cover letter as text`() = runTest {
        coEvery { coverLetterRepository.getCoverLetterById(1L) } returns flowOf(Result.Success(sampleLetter()))

        val result = useCase(ExportCoverLetterRequest(coverLetterId = 1L, format = ExportLetterFormat.TXT))
        assertTrue(result.isSuccess)
        val text = (result as Result.Success).data
        assertTrue(text.contains("Tech Corp"))
        assertTrue(text.contains("Dear Hiring Manager..."))
    }

    @Test
    fun `should export cover letter as markdown`() = runTest {
        coEvery { coverLetterRepository.getCoverLetterById(1L) } returns flowOf(Result.Success(sampleLetter()))

        val result = useCase(ExportCoverLetterRequest(coverLetterId = 1L, format = ExportLetterFormat.MARKDOWN))
        assertTrue(result.isSuccess)
        val text = (result as Result.Success).data
        assertTrue(text.startsWith("#"))
    }

    @Test
    fun `should fail for invalid cover letter ID`() = runTest {
        val result = useCase(ExportCoverLetterRequest(coverLetterId = 0L))
        assertTrue(result.isFailure)
    }
}
