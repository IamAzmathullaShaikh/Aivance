package com.bangersoul.aivance.feature.profile

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.AppSettings as CoreAppSettings
import com.bangersoul.aivance.core.domain.usecase.settings.LoadSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.SaveSettingsUseCase
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
class NotificationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockLoadSettings: LoadSettingsUseCase = mockk()
    private val mockSaveSettings: SaveSettingsUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private fun createViewModel() = NotificationsViewModel(mockLoadSettings, mockSaveSettings, mockTrackEvent)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any()) } returns Result.Success(Unit)
        coEvery { mockLoadSettings.invoke() } returns flowOf(Result.Success(CoreAppSettings()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Success`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value is NotificationsUiState.Success)
        assertEquals(0, (vm.uiState.value as NotificationsUiState.Success).unreadCount)
    }

    @Test
    fun `markAllAsRead sends snackbar effect`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(NotificationsUiEvent.MarkAllAsRead)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.effects.test {
            val effect = awaitItem()
            assertTrue(effect is NotificationsUiEffect.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleJobAlerts updates state and tracks event`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(NotificationsUiEvent.ToggleJobAlerts(false))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is NotificationsUiState.Success)
        assertEquals(false, (state as NotificationsUiState.Success).jobAlertsEnabled)
        coVerify { mockTrackEvent(TrackEventRequest("notifications_toggle_job_alerts_false")) }
    }

    @Test
    fun `toggleFollowUpReminders updates state`() = runTest {
        val vm = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(NotificationsUiEvent.ToggleFollowUpReminders(false))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is NotificationsUiState.Success)
        assertEquals(false, (state as NotificationsUiState.Success).followUpRemindersEnabled)
        coVerify { mockTrackEvent(TrackEventRequest("notifications_toggle_followup_false")) }
    }
}
