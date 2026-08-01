package com.bangersoul.aivance.feature.dashboard

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: HomeViewModel

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
    fun `initial state is Success with greeting`() {
        viewModel = HomeViewModel(mockTrackEvent)
        assertTrue(viewModel.uiState.value is HomeUiState.Success)
    }

    @Test
    fun `quick action navigates to correct route`() = runTest(testDispatcher) {
        viewModel = HomeViewModel(mockTrackEvent)

        viewModel.onEvent(HomeUiEvent.PerformQuickAction(QuickAction.AnalyzeResume))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            val effect = awaitItem()
            assertTrue(effect is HomeUiEffect.Navigate)
            assertEquals("resume", (effect as HomeUiEffect.Navigate).route)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `quick action triggers track event`() = runTest(testDispatcher) {
        viewModel = HomeViewModel(mockTrackEvent)

        viewModel.onEvent(HomeUiEvent.PerformQuickAction(QuickAction.SearchJobs))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockTrackEvent.invoke(match { it.eventName == "quick_action_SearchJobs" }) }
    }

    @Test
    fun `navigate to dashboard triggers effect`() = runTest(testDispatcher) {
        viewModel = HomeViewModel(mockTrackEvent)

        viewModel.onEvent(HomeUiEvent.NavigateToDashboard)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            val effect = awaitItem()
            assertTrue(effect is HomeUiEffect.Navigate)
            assertEquals("dashboard", (effect as HomeUiEffect.Navigate).route)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `show notifications triggers navigate effect`() = runTest(testDispatcher) {
        viewModel = HomeViewModel(mockTrackEvent)

        viewModel.onEvent(HomeUiEvent.ShowNotifications)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            val effect = awaitItem()
            assertTrue(effect is HomeUiEffect.Navigate)
            assertEquals("notifications", (effect as HomeUiEffect.Navigate).route)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `greeting returns correct message for time of day`() {
        // This should always return one of the three greetings
        val greeting = getGreeting()
        assertTrue(greeting in listOf("Good morning", "Good afternoon", "Good evening"))
    }
}
