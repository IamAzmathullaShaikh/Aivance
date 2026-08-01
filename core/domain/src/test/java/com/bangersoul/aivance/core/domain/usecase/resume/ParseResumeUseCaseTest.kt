package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.result.DatabaseError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
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
    fun `should parse resume successfully`() = runTest {
        coEvery { resumeRepository.parseResume(1L) } returns Result.Success(Unit)

        val result = useCase(1L)

        assertTrue(result.isSuccess)
        coVerify { resumeRepository.parseResume(1L) }
    }

    @Test
    fun `should propagate repository failure`() = runTest {
        coEvery { resumeRepository.parseResume(1L) } returns Result.Failure(DatabaseError("Parse failed"))

        val result = useCase(1L)

        assertTrue(result.isFailure)
    }
}
