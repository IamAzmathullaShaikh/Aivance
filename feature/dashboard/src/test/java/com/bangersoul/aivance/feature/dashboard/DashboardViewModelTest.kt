package com.bangersoul.aivance.feature.dashboard

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.feature.dashboard.domain.DashboardData
import com.bangersoul.aivance.feature.dashboard.domain.DashboardRepository
import com.bangersoul.aivance.feature.dashboard.domain.ResumeStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: DashboardRepository = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: DashboardViewModel

    private fun sampleData() = DashboardData(
        profileCompletion = 75,
        resumeStatus = ResumeStatus(fileName = "resume.pdf", uploadedDate = LocalDate.now()),
        atsScore = 85,
        activeApplications = 5,
        interviewPrepStatus = "Scheduled"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent.invoke(any()) } returns Result.Success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state becomes Loading`() {
        every { mockRepository.getDashboardData() } returns MutableStateFlow(sampleData())

        viewModel = DashboardViewModel(mockRepository, mockTrackEvent)

        assertTrue(viewModel.uiState.value is DashboardUiState.Loading)
    }

    @Test
    fun `repository emits data then state becomes Success`() = runTest(testDispatcher) {
        every { mockRepository.getDashboardData() } returns MutableStateFlow(sampleData())

        viewModel = DashboardViewModel(mockRepository, mockTrackEvent)

        viewModel.uiState.test {
            // Skip initial Loading
            skipItems(1)
            val successState = awaitItem()
            assertTrue(successState is DashboardUiState.Success)
            val success = successState as DashboardUiState.Success
            assertEquals(85, success.recentAtsScore)
            assertEquals(5, success.activeApplicationCount)
            assertEquals(0.75f, success.profileCompletionPercent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigation events send correct effects`() = runTest(testDispatcher) {
        every { mockRepository.getDashboardData() } returns MutableStateFlow(sampleData())

        viewModel = DashboardViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(DashboardUiEvent.NavigateToResume)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            val effect = awaitItem()
            assertTrue(effect is DashboardUiEffect.NavigateTo)
            assertEquals("resume", (effect as DashboardUiEffect.NavigateTo).route)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh triggers track event`() = runTest(testDispatcher) {
        every { mockRepository.getDashboardData() } returns MutableStateFlow(sampleData())

        viewModel = DashboardViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(DashboardUiEvent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockTrackEvent.invoke(match { it.eventName == "dashboard_refresh" }) }
    }

    @Test
    fun `settings event opens settings`() = runTest(testDispatcher) {
        every { mockRepository.getDashboardData() } returns MutableStateFlow(sampleData())

        viewModel = DashboardViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(DashboardUiEvent.NavigateToSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            assertTrue(awaitItem() is DashboardUiEffect.OpenSettings)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
