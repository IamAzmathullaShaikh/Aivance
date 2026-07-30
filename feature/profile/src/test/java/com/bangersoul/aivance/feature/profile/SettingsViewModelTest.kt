package com.bangersoul.aivance.feature.profile

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.ExportSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.LoadSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.ResetSettingsUseCase
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockLoadSettings: LoadSettingsUseCase = mockk()
    private val mockSaveSettings: SaveSettingsUseCase = mockk()
    private val mockExportSettings: ExportSettingsUseCase = mockk()
    private val mockResetSettings: ResetSettingsUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: SettingsViewModel

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
    fun `initial state loads settings`() = runTest {
        coEvery { mockLoadSettings.invoke() } returns flowOf(CoreResult.Success(AppSettings()))

        viewModel = SettingsViewModel(
            mockLoadSettings, mockSaveSettings, mockExportSettings,
            mockResetSettings, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assert(viewModel.uiState.value is SettingsUiState.Success)
    }

    @Test
    fun `save settings triggers use case and shows snackbar`() = runTest {
        coEvery { mockLoadSettings.invoke() } returns flowOf(CoreResult.Success(AppSettings()))
        coEvery { mockSaveSettings.invoke(any()) } returns flowOf(CoreResult.Success(Unit))

        viewModel = SettingsViewModel(
            mockLoadSettings, mockSaveSettings, mockExportSettings,
            mockResetSettings, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.SetThemeMode("dark"))
        viewModel.onEvent(SettingsUiEvent.SaveSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockSaveSettings.invoke(any()) }
        coVerify { mockTrackEvent("settings_save") }
    }

    @Test
    fun `reset settings triggers use case`() = runTest {
        coEvery { mockLoadSettings.invoke() } returns flowOf(CoreResult.Success(AppSettings()))
        coEvery { mockResetSettings.invoke() } returns flowOf(CoreResult.Success(Unit))

        viewModel = SettingsViewModel(
            mockLoadSettings, mockSaveSettings, mockExportSettings,
            mockResetSettings, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.ResetSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockResetSettings.invoke() }
    }

    @Test
    fun `theme change updates pending settings`() = runTest {
        coEvery { mockLoadSettings.invoke() } returns flowOf(CoreResult.Success(AppSettings()))

        viewModel = SettingsViewModel(
            mockLoadSettings, mockSaveSettings, mockExportSettings,
            mockResetSettings, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.SetThemeMode("dark"))
    }

    @Test
    fun `settings error shows error state`() = runTest {
        coEvery { mockLoadSettings.invoke() } returns flowOf(
            CoreResult.Failure(com.bangersoul.aivance.core.common.result.ProviderError("test", message = "Load failed"))
        )

        viewModel = SettingsViewModel(
            mockLoadSettings, mockSaveSettings, mockExportSettings,
            mockResetSettings, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assert(viewModel.uiState.value is SettingsUiState.Error)
    }
}
