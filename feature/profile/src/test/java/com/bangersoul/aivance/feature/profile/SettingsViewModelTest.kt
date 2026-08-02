package com.bangersoul.aivance.feature.profile

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.datastore.UserPreferences
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.ExportSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.LoadSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.ResetSettingsUseCase
import com.bangersoul.aivance.core.domain.usecase.settings.SaveSettingsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
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
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockLoadSettings: LoadSettingsUseCase = mockk()
    private val mockSaveSettings: SaveSettingsUseCase = mockk()
    private val mockExportSettings: ExportSettingsUseCase = mockk()
    private val mockResetSettings: ResetSettingsUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()
    private val mockUserPreferences: UserPreferencesRepository = mockk()

    private fun createViewModel() = SettingsViewModel(
        mockLoadSettings, mockSaveSettings, mockExportSettings,
        mockResetSettings, mockTrackEvent, mockUserPreferences
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any()) } returns Result.Success(Unit)
        every { mockUserPreferences.userPreferences } returns flowOf(UserPreferences())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads with defaults`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is SettingsUiState.Success)
        assertEquals("system", (state as SettingsUiState.Success).settings.themeMode)
        assertEquals(true, state.settings.dynamicColorEnabled)
    }

    @Test
    fun `theme change is applied to state after save`() = runTest {
        coEvery { mockSaveSettings.invoke(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.SetThemeMode("dark"))
        viewModel.onEvent(SettingsUiEvent.SaveSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("dark", (viewModel.uiState.value as SettingsUiState.Success).settings.themeMode)
    }

    @Test
    fun `save settings triggers use case and tracks event`() = runTest {
        coEvery { mockSaveSettings.invoke(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.SetThemeMode("dark"))
        viewModel.onEvent(SettingsUiEvent.SaveSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockSaveSettings.invoke(any()) }
        coVerify { mockTrackEvent(TrackEventRequest(eventName = "settings_save")) }
    }

    @Test
    fun `reset settings triggers use case and restores defaults`() = runTest {
        coEvery { mockResetSettings.invoke() } returns Result.Success(Unit)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.SetThemeMode("dark"))
        viewModel.onEvent(SettingsUiEvent.ResetSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockResetSettings.invoke() }
        assertEquals("system", (viewModel.uiState.value as SettingsUiState.Success).settings.themeMode)
    }

    @Test
    fun `set language persists via repository and updates state`() = runTest {
        coEvery { mockUserPreferences.updateLanguage("es") } returns Unit

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.SetLanguage("es"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockUserPreferences.updateLanguage("es") }
        assertEquals("es", (viewModel.uiState.value as SettingsUiState.Success).settings.language)
    }

    @Test
    fun `initial load reads persisted language`() = runTest {
        every { mockUserPreferences.userPreferences } returns flowOf(
            UserPreferences(language = "fr")
        )

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("fr", (viewModel.uiState.value as SettingsUiState.Success).settings.language)
    }

    @Test
    fun `export settings emits export result effect`() = runTest {
        coEvery { mockExportSettings.invoke() } returns Result.Success("/tmp/settings.json")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(SettingsUiEvent.ExportSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            val effect = awaitItem()
            assertTrue(effect is SettingsUiEffect.ExportResult)
            assertEquals("/tmp/settings.json", (effect as SettingsUiEffect.ExportResult).path)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
