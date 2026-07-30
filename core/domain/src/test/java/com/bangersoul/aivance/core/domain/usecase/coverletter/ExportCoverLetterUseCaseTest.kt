package com.bangersoul.aivance.core.domain.usecase.coverletter

import com.bangersoul.aivance.core.common.enums.LetterTone
import com.bangersoul.aivance.core.common.model.CoverLetter
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

    @Test
    fun `should export cover letter as text`() = runTest {
        val letter = CoverLetter(id = 1L, company = "Tech Corp", role = "Engineer", content = "Dear Hiring Manager...", tone = LetterTone.PROFESSIONAL)
        coEvery { coverLetterRepository.getCoverLetterById(1L) } returns flowOf(Result.Success(letter))

        val result = useCase(ExportCoverLetterRequest(coverLetterId = 1L, format = ExportLetterFormat.TXT))
        assertTrue(result.isSuccess)
        val text = (result as Result.Success).data
        assertTrue(text.contains("Tech Corp"))
    }

    @Test
    fun `should export cover letter as markdown`() = runTest {
        val letter = CoverLetter(id = 1L, company = "Tech Corp", role = "Engineer", content = "Dear Hiring Manager...", tone = LetterTone.PROFESSIONAL)
        coEvery { coverLetterRepository.getCoverLetterById(1L) } returns flowOf(Result.Success(letter))

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
