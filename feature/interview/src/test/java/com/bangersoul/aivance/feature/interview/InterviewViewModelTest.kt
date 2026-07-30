package com.bangersoul.aivance.feature.interview

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.InterviewSession as DomainInterviewSession
import com.bangersoul.aivance.core.common.model.InterviewFeedback as DomainInterviewFeedback
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.EndInterviewUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.EvaluateAnswersUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.GenerateFeedbackUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.GenerateInterviewQuestionsUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.StartInterviewSessionUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InterviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockStartSession: StartInterviewSessionUseCase = mockk()
    private val mockGenerateQuestions: GenerateInterviewQuestionsUseCase = mockk()
    private val mockEvaluateAnswers: EvaluateAnswersUseCase = mockk()
    private val mockGenerateFeedback: GenerateFeedbackUseCase = mockk()
    private val mockEndInterview: EndInterviewUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: InterviewViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any()) } returns flowOf(Result.Success(Unit))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        viewModel = InterviewViewModel(
            mockStartSession, mockGenerateQuestions, mockEvaluateAnswers,
            mockGenerateFeedback, mockEndInterview, mockTrackEvent
        )
        assert(viewModel.uiState.value is InterviewUiState.Idle)
    }

    @Test
    fun `starting session with empty role shows error`() {
        viewModel = InterviewViewModel(
            mockStartSession, mockGenerateQuestions, mockEvaluateAnswers,
            mockGenerateFeedback, mockEndInterview, mockTrackEvent
        )
        viewModel.onEvent(InterviewUiEvent.StartSession(""))
        assert(viewModel.uiState.value is InterviewUiState.Error)
    }

    @Test
    fun `start session transitions through states correctly`() = runTest {
        val session = DomainInterviewSession(id = "session_1", role = "Android Dev", difficulty = "MEDIUM")
        coEvery { mockStartSession.invoke(any(), any()) } returns flowOf(Result.Success(session))

        viewModel = InterviewViewModel(
            mockStartSession, mockGenerateQuestions, mockEvaluateAnswers,
            mockGenerateFeedback, mockEndInterview, mockTrackEvent
        )

        viewModel.onEvent(InterviewUiEvent.StartSession("Android Dev", "MEDIUM"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            skipItems(1) // Preparing
            skipItems(1) // Loading
            val ready = awaitItem()
            assert(ready is InterviewUiState.Ready)
            assert((ready as InterviewUiState.Ready).sessionId == "session_1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `end session transitions to feedback state`() = runTest {
        val session = DomainInterviewSession(id = "session_1", role = "Test", difficulty = "EASY")
        val feedback = DomainInterviewFeedback(overallScore = 85, strengths = listOf("Communication"), improvements = listOf("Technical depth"))
        coEvery { mockStartSession.invoke(any(), any()) } returns flowOf(Result.Success(session))
        coEvery { mockEndInterview.invoke(any()) } returns flowOf(Result.Success(feedback))

        viewModel = InterviewViewModel(
            mockStartSession, mockGenerateQuestions, mockEvaluateAnswers,
            mockGenerateFeedback, mockEndInterview, mockTrackEvent
        )

        viewModel.onEvent(InterviewUiEvent.StartSession("Test", "EASY"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(InterviewUiEvent.EndSession("session_1"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val feedbackState = awaitItem()
            assert(feedbackState is InterviewUiState.Feedback)
            assert((feedbackState as InterviewUiState.Feedback).feedback.summary == "85")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reset returns to Idle`() {
        viewModel = InterviewViewModel(
            mockStartSession, mockGenerateQuestions, mockEvaluateAnswers,
            mockGenerateFeedback, mockEndInterview, mockTrackEvent
        )
        viewModel.onEvent(InterviewUiEvent.Reset)
        assert(viewModel.uiState.value is InterviewUiState.Idle)
    }
}
