package com.bangersoul.aivance.feature.dashboard

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any()) } returns flowOf(CoreResult.Success(Unit))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Success with greeting`() {
        viewModel = HomeViewModel(mockTrackEvent)
        assert(viewModel.uiState.value is HomeUiState.Success)
    }

    @Test
    fun `quick action navigates to correct route`() = runTest {
        viewModel = HomeViewModel(mockTrackEvent)

        viewModel.onEvent(HomeUiEvent.QuickAction(QuickAction.AnalyzeResume))

        viewModel.effects.test {
            val effect = awaitItem()
            assert(effect is HomeUiEffect.Navigate)
            assert((effect as HomeUiEffect.Navigate).route == "resume")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `quick action triggers track event`() = runTest {
        viewModel = HomeViewModel(mockTrackEvent)

        viewModel.onEvent(HomeUiEvent.QuickAction(QuickAction.SearchJobs))

        coVerify { mockTrackEvent("quick_action_SearchJobs") }
    }

    @Test
    fun `navigate to dashboard triggers effect`() = runTest {
        viewModel = HomeViewModel(mockTrackEvent)

        viewModel.onEvent(HomeUiEvent.NavigateToDashboard)

        viewModel.effects.test {
            val effect = awaitItem()
            assert(effect is HomeUiEffect.Navigate)
            assert((effect as HomeUiEffect.Navigate).route == "dashboard")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `show notifications triggers navigate effect`() = runTest {
        viewModel = HomeViewModel(mockTrackEvent)

        viewModel.onEvent(HomeUiEvent.ShowNotifications)

        viewModel.effects.test {
            val effect = awaitItem()
            assert(effect is HomeUiEffect.Navigate)
            assert((effect as HomeUiEffect.Navigate).route == "notifications")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `greeting returns correct message for time of day`() {
        // This should always return one of the three greetings
        val greeting = getGreeting()
        assert(greeting in listOf("Good morning", "Good afternoon", "Good evening"))
    }
}
