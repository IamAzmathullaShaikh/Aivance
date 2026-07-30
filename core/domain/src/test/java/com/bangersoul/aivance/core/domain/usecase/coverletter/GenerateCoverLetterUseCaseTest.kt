package com.bangersoul.aivance.core.domain.usecase.coverletter

import com.bangersoul.aivance.core.common.enums.LetterTone
import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateCoverLetterUseCaseTest {

    private lateinit var coverLetterRepository: CoverLetterRepository
    private lateinit var useCase: GenerateCoverLetterUseCase

    @Before
    fun setUp() {
        coverLetterRepository = mockk()
        useCase = GenerateCoverLetterUseCase(coverLetterRepository)
    }

    @Test
    fun `should generate cover letter successfully`() = runTest {
        val letter = CoverLetter(company = "Tech Corp", role = "Engineer", content = "Dear Hiring Manager...", tone = LetterTone.PROFESSIONAL)
        coEvery { coverLetterRepository.generateCoverLetter(any(), any(), any()) } returns Result.Success(letter)

        val result = useCase(GenerateCoverLetterRequest(companyName = "Tech Corp", role = "Engineer", jobDescription = "Job desc for experienced engineer"))

        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank company name`() = runTest {
        val result = useCase(GenerateCoverLetterRequest(companyName = "", role = "Engineer", jobDescription = "test"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail for blank role`() = runTest {
        val result = useCase(GenerateCoverLetterRequest(companyName = "Tech Corp", role = "", jobDescription = "test"))
        assertTrue(result.isFailure)
    }
}
