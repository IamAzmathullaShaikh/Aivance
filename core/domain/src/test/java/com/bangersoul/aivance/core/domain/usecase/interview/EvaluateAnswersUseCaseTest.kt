package com.bangersoul.aivance.core.domain.usecase.interview

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EvaluateAnswersUseCaseTest {

    private lateinit var interviewRepository: InterviewRepository
    private lateinit var aiRepository: AiRepository
    private lateinit var useCase: EvaluateAnswersUseCase

    @Before
    fun setUp() {
        interviewRepository = mockk()
        aiRepository = mockk()
        useCase = EvaluateAnswersUseCase(interviewRepository, aiRepository)
    }

    @Test
    fun `should evaluate answer successfully`() = runTest {
        val aiResponse = "SCORE: 85 | FEEDBACK: Good answer | SUGGESTIONS: Be more specific, Add examples"
        coEvery { aiRepository.analyzeText(any(), any()) } returns Result.Success(aiResponse)

        val result = useCase(EvaluateAnswersRequest(sessionId = "session1", question = "Tell me about yourself", answer = "I am a developer"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank session ID`() = runTest {
        val result = useCase(EvaluateAnswersRequest(sessionId = "", question = "Q", answer = "A"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail for blank question`() = runTest {
        val result = useCase(EvaluateAnswersRequest(sessionId = "s1", question = "", answer = "A"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail for blank answer`() = runTest {
        val result = useCase(EvaluateAnswersRequest(sessionId = "s1", question = "Q", answer = ""))
        assertTrue(result.isFailure)
    }
}
