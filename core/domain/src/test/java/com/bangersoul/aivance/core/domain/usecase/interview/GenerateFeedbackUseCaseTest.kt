package com.bangersoul.aivance.core.domain.usecase.interview

import com.bangersoul.aivance.core.common.model.InterviewFeedback
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateFeedbackUseCaseTest {

    private lateinit var interviewRepository: InterviewRepository
    private lateinit var useCase: GenerateFeedbackUseCase

    @Before
    fun setUp() {
        interviewRepository = mockk()
        useCase = GenerateFeedbackUseCase(interviewRepository)
    }

    @Test
    fun `should generate feedback successfully`() = runTest {
        val feedback = InterviewFeedback(overallScore = 85, strengths = listOf("Communication"), improvements = listOf("Technical depth"))
        coEvery { interviewRepository.generateFeedback(any()) } returns Result.Success(feedback)

        val result = useCase("session1")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank session ID`() = runTest {
        val result = useCase("")
        assertTrue(result.isFailure)
    }
}
