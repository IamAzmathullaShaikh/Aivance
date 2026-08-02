package com.bangersoul.aivance.feature.tracker

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.Application
import com.bangersoul.aivance.core.common.model.ApplicationStage
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import com.bangersoul.aivance.core.domain.repository.ApplicationWorkflowRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.workflow.WorkflowEngine
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: ApplicationWorkflowRepository = mockk()
    private val mockAnalyticsRepository: AnalyticsRepository = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: TrackerViewModel

    private fun buildViewModel(): TrackerViewModel {
        val workflowEngine = WorkflowEngine(mockRepository, mockAnalyticsRepository)
        return TrackerViewModel(mockRepository, workflowEngine, mockTrackEvent)
    }

    private val stages = listOf(
        ApplicationStage(id = "SAVED", label = "Saved", order = 1),
        ApplicationStage(id = "APPLIED", label = "Applied", order = 2),
        ApplicationStage(id = "INTERVIEWING", label = "Interviewing", order = 3)
    )

    private fun sampleApp(stageId: String = "SAVED") = Application(
        id = 1L,
        jobId = 10L,
        currentStageId = stageId,
        status = "ACTIVE"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any()) } returns Result.Success(Unit)
        coEvery { mockAnalyticsRepository.createSnapshot() } returns Result.Success(1L)
        coEvery { mockRepository.getStages() } returns flowOf(Result.Success(stages))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        coEvery { mockRepository.getApplications() } returns flowOf(Result.Success(emptyList()))

        viewModel = buildViewModel()

        assertEquals(TrackerUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `empty applications show Success with empty list`() = runTest {
        coEvery { mockRepository.getApplications() } returns flowOf(Result.Success(emptyList()))

        viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is TrackerUiState.Success)
        assertEquals(0, (state as TrackerUiState.Success).applications.size)
    }

    @Test
    fun `applications show in Success state`() = runTest {
        coEvery { mockRepository.getApplications() } returns flowOf(Result.Success(listOf(sampleApp())))

        viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is TrackerUiState.Success)
        assertEquals(1, (state as TrackerUiState.Success).applications.size)
        assertEquals(stages.size, (state as TrackerUiState.Success).stages.size)
    }

    @Test
    fun `error when repository fails to load`() = runTest {
        coEvery { mockRepository.getApplications() } returns flowOf(
            Result.Failure(com.bangersoul.aivance.core.common.result.DomainError("Failed"))
        )

        viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is TrackerUiState.Error)
    }

    @Test
    fun `update stage saves application and tracks event`() = runTest {
        coEvery { mockRepository.getApplications() } returns flowOf(Result.Success(listOf(sampleApp())))
        coEvery { mockRepository.getApplicationById(1L) } returns flowOf(Result.Success(sampleApp()))
        coEvery { mockRepository.saveApplication(any()) } returns Result.Success(1L)
        coEvery { mockRepository.addTimelineEvent(any()) } returns Result.Success(1L)

        viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(TrackerUiEvent.UpdateStage(applicationId = 1L, stageId = "INTERVIEWING"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepository.saveApplication(any()) }
        coVerify { mockRepository.addTimelineEvent(any()) }
        coVerify { mockTrackEvent(TrackEventRequest("tracker_stage_update")) }
    }

    @Test
    fun `delete application calls repository`() = runTest {
        coEvery { mockRepository.getApplications() } returns flowOf(Result.Success(listOf(sampleApp())))
        coEvery { mockRepository.deleteApplication(1L) } returns Result.Success(Unit)

        viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(TrackerUiEvent.DeleteApplication(id = 1L))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepository.deleteApplication(1L) }
    }

    @Test
    fun `refresh reloads applications`() = runTest {
        coEvery { mockRepository.getApplications() } returns flowOf(Result.Success(listOf(sampleApp())))

        viewModel = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { mockRepository.getApplications() } returns flowOf(Result.Success(emptyList()))
        viewModel.onEvent(TrackerUiEvent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is TrackerUiState.Success)
        assertEquals(0, (state as TrackerUiState.Success).applications.size)
    }
}
