package com.bangersoul.aivance.feature.interview

import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.model.CareerState
import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.engine.CareerStateEngine
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.core.domain.repository.crm.CompanyIntelligenceRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InterviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: InterviewRepository = mockk()
    private val mockCareerStateEngine: CareerStateEngine = mockk()
    private val mockCompanyRepository: CompanyIntelligenceRepository = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: InterviewViewModel

    private val sampleSession = InterviewSession(
        id = "session_1",
        targetRole = "Android Dev",
        type = "BEHAVIORAL",
        companyName = "Tech Corp",
        difficulty = InterviewDifficulty.MEDIUM
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent.invoke(any()) } returns Result.Success(Unit)
        every { mockCareerStateEngine.state } returns MutableStateFlow(CareerState())
        coEvery {
            mockRepository.startSession(any(), any(), any(), any(), any(), any())
        } returns Result.Success(sampleSession)
        coEvery { mockRepository.generateQuestions(any(), any()) } returns Result.Success(Unit)
        // History is loaded in init — provide an empty session list by default.
        every { mockRepository.getSessions() } returns flowOf(Result.Success(emptyList()))
        every { mockRepository.getQuestions(any()) } returns flowOf(Result.Success(emptyList()))
        every { mockRepository.getSessionById(any()) } returns flowOf(Result.Success(sampleSession))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        viewModel = InterviewViewModel(
            mockRepository, mockCareerStateEngine, mockCompanyRepository, mockTrackEvent
        )
        assertTrue(viewModel.uiState.value is InterviewUiState.Idle)
    }

    @Test
    fun `start session transitions to Active`() = runTest(testDispatcher) {
        viewModel = InterviewViewModel(
            mockRepository, mockCareerStateEngine, mockCompanyRepository, mockTrackEvent
        )

        viewModel.onEvent(InterviewUiEvent.StartSession("Android Dev", "Tech Corp", "BEHAVIORAL"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is InterviewUiState.Active)
        assertEquals("session_1", (state as InterviewUiState.Active).session.id)
    }

    @Test
    fun `start session failure shows error`() = runTest(testDispatcher) {
        coEvery {
            mockRepository.startSession(any(), any(), any(), any(), any(), any())
        } returns Result.Failure(DomainError("Failed to start session"))

        viewModel = InterviewViewModel(
            mockRepository, mockCareerStateEngine, mockCompanyRepository, mockTrackEvent
        )

        viewModel.onEvent(InterviewUiEvent.StartSession("Android Dev", "Tech Corp", "BEHAVIORAL"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is InterviewUiState.Error)
    }

    @Test
    fun `complete session transitions to Review`() = runTest(testDispatcher) {
        coEvery { mockRepository.completeSession(any()) } returns Result.Success(Unit)

        viewModel = InterviewViewModel(
            mockRepository, mockCareerStateEngine, mockCompanyRepository, mockTrackEvent
        )

        viewModel.onEvent(InterviewUiEvent.StartSession("Android Dev", "Tech Corp", "BEHAVIORAL"))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(InterviewUiEvent.Complete)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is InterviewUiState.Review)
    }

    @Test
    fun `next question increments index`() = runTest(testDispatcher) {
        viewModel = InterviewViewModel(
            mockRepository, mockCareerStateEngine, mockCompanyRepository, mockTrackEvent
        )

        viewModel.onEvent(InterviewUiEvent.StartSession("Android Dev", "Tech Corp", "BEHAVIORAL"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(InterviewUiEvent.NextQuestion)

        val state = viewModel.uiState.value
        assertTrue(state is InterviewUiState.Active)
        assertEquals(1, (state as InterviewUiState.Active).currentQuestionIndex)
    }

    @Test
    fun `reset returns to Idle`() {
        viewModel = InterviewViewModel(
            mockRepository, mockCareerStateEngine, mockCompanyRepository, mockTrackEvent
        )
        viewModel.onEvent(InterviewUiEvent.Reset)
        assertTrue(viewModel.uiState.value is InterviewUiState.Idle)
    }

    @Test
    fun `load history populates Idle state with past sessions`() = runTest(testDispatcher) {
        every { mockRepository.getSessions() } returns flowOf(
            Result.Success(listOf(sampleSession.copy(isCompleted = true)))
        )

        viewModel = InterviewViewModel(
            mockRepository, mockCareerStateEngine, mockCompanyRepository, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is InterviewUiState.Idle)
        assertEquals(1, (state as InterviewUiState.Idle).history.size)
    }
}
