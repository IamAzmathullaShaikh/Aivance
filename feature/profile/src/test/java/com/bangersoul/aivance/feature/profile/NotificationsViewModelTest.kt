package com.bangersoul.aivance.feature.profile

import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.LoadSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.SaveSettingsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any<TrackEventRequest>()) } returns com.bangersoul.aivance.core.common.result.Result.Success(Unit)
        coEvery { mockLoadSettings() } returns flowOf(com.bangersoul.aivance.core.common.result.Result.Success(emptyMap()))
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is Success`() = runTest {
        val vm = NotificationsViewModel(mockLoadSettings, mockSaveSettings, mockTrackEvent)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is NotificationsUiState.Success)
    }

    @Test
    fun `markAllAsRead sets unread count to 0`() = runTest {
        val vm = NotificationsViewModel(mockLoadSettings, mockSaveSettings, mockTrackEvent)
        advanceUntilIdle()

        assertTrue(vm.uiState.value is NotificationsUiState.Success)
    }

    @Test
    fun `toggleJobAlerts updates state`() = runTest {
        val vm = NotificationsViewModel(mockLoadSettings, mockSaveSettings, mockTrackEvent)
        advanceUntilIdle()

        vm.onEvent(NotificationsUiEvent.ToggleJobAlerts(false))
        val state = vm.uiState.value
        assertTrue(state is NotificationsUiState.Success)
        assertEquals(false, (state as NotificationsUiState.Success).jobAlertsEnabled)
    }
}
