package com.bangersoul.aivance.core.domain.usecase.coverletter

import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
        val original = CoverLetter(
            id = 1L,
            resumeVersionId = 2L,
            jobId = 3L,
            recruiterId = "recruiter_1",
            company = "Tech Corp",
            role = "Engineer"
        )
        coEvery { coverLetterRepository.getCoverLetterById(1L) } returns flowOf(Result.Success(original))
        coEvery { coverLetterRepository.generateCoverLetter(any(), any(), any(), any(), any()) } returns Result.Success(99L)

        val result = useCase(ImproveCoverLetterRequest(coverLetterId = 1L, feedback = "Make it more enthusiastic"))

        assertTrue(result.isSuccess)
        assertEquals(99L, (result as Result.Success).data)
        coVerify {
            coverLetterRepository.generateCoverLetter(
                resumeId = 0L,
                resumeVersionId = 2L,
                jobId = 3L,
                recruiterId = "recruiter_1",
                writingStyle = "IMPROVED: Make it more enthusiastic"
            )
        }
    }

    @Test
    fun `should fail for invalid cover letter ID`() = runTest {
        val result = useCase(ImproveCoverLetterRequest(coverLetterId = 0L))

        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail when cover letter is missing`() = runTest {
        coEvery { coverLetterRepository.getCoverLetterById(1L) } returns flowOf(Result.Failure(com.bangersoul.aivance.core.common.result.DomainError("Not found")))

        val result = useCase(ImproveCoverLetterRequest(coverLetterId = 1L, feedback = "Improve"))

        assertTrue(result.isFailure)
    }
}
