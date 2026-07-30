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

class ImproveCoverLetterUseCaseTest {

    private lateinit var coverLetterRepository: CoverLetterRepository
    private lateinit var useCase: ImproveCoverLetterUseCase

    @Before
    fun setUp() {
        coverLetterRepository = mockk()
        useCase = ImproveCoverLetterUseCase(coverLetterRepository)
    }

    @Test
    fun `should improve cover letter with feedback`() = runTest {
        val original = CoverLetter(id = 1L, company = "Tech Corp", role = "Engineer", content = "Old content", tone = LetterTone.PROFESSIONAL)
        val improved = CoverLetter(company = "Tech Corp", role = "Engineer", content = "Improved content", tone = LetterTone.PROFESSIONAL)

        coEvery { coverLetterRepository.getCoverLetterById(1L) } returns flowOf(Result.Success(original))
        coEvery { coverLetterRepository.generateCoverLetter(any(), any(), any()) } returns Result.Success(improved)

        val result = useCase(ImproveCoverLetterRequest(coverLetterId = 1L, feedback = "Make it more enthusiastic"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for invalid cover letter ID`() = runTest {
        val result = useCase(ImproveCoverLetterRequest(coverLetterId = 0L))
        assertTrue(result.isFailure)
    }
}
