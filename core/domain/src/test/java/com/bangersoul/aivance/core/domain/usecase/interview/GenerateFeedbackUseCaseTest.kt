package com.bangersoul.aivance.core.domain.usecase.interview

import com.bangersoul.aivance.core.common.model.InterviewFeedback
import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
    fun `should return feedback from session`() = runTest {
        val feedback = InterviewFeedback(
            overallScore = 85,
            strengths = listOf("Communication"),
            improvements = listOf("Technical depth")
        )
        val session = InterviewSession(id = "session1", targetRole = "Engineer", feedback = feedback)
        coEvery { interviewRepository.getSessionById("session1") } returns flowOf(Result.Success(session))

        val result = useCase("session1")

        assertTrue(result.isSuccess)
        assertEquals(85, (result as Result.Success).data.overallScore)
    }

    @Test
    fun `should fail when session is missing`() = runTest {
        coEvery { interviewRepository.getSessionById("missing") } returns flowOf(Result.Failure(DomainError("Session not found")))

        val result = useCase("missing")

        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail when feedback not yet generated`() = runTest {
        val session = InterviewSession(id = "session1", targetRole = "Engineer", feedback = null)
        coEvery { interviewRepository.getSessionById("session1") } returns flowOf(Result.Success(session))

        val result = useCase("session1")

        assertTrue(result.isFailure)
    }
}
