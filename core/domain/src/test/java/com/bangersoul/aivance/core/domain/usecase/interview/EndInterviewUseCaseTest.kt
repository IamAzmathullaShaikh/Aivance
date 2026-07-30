package com.bangersoul.aivance.core.domain.usecase.interview

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EndInterviewUseCaseTest {

    private lateinit var interviewRepository: InterviewRepository
    private lateinit var useCase: EndInterviewUseCase

    @Before
    fun setUp() {
        interviewRepository = mockk()
        useCase = EndInterviewUseCase(interviewRepository)
    }

    @Test
    fun `should end session successfully`() = runTest {
        coEvery { interviewRepository.completeSession(any()) } returns Result.Success(Unit)

        val result = useCase("session1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank session ID`() = runTest {
        val result = useCase("")
        assertTrue(result.isFailure)
    }
}
