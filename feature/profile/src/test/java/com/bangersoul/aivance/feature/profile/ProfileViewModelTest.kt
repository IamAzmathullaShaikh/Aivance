package com.bangersoul.aivance.feature.profile

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.user.CreateProfileUseCase
import com.bangersoul.aivance.core.domain.usecase.user.DeleteProfileUseCase
import com.bangersoul.aivance.core.domain.usecase.user.LoadProfileUseCase
import com.bangersoul.aivance.core.domain.usecase.user.UpdateProfileUseCase
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
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockLoadProfile: LoadProfileUseCase = mockk()
    private val mockCreateProfile: CreateProfileUseCase = mockk()
    private val mockUpdateProfile: UpdateProfileUseCase = mockk()
    private val mockDeleteProfile: DeleteProfileUseCase = mockk()
    private val mockUserPrefs: UserPreferencesRepository = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private val sampleProfile = UserProfile(
        fullName = "John Doe",
        email = "john@example.com",
        targetRole = "Android Engineer",
        skills = listOf("Kotlin", "Compose"),
        experienceYears = 5
    )

    private fun createViewModel() = ProfileViewModel(
        mockLoadProfile, mockCreateProfile, mockUpdateProfile,
        mockDeleteProfile, mockUserPrefs, mockTrackEvent
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any()) } returns Result.Success(Unit)
        coEvery { mockLoadProfile.invoke() } returns flowOf(Result.Success(sampleProfile))
        coEvery { mockUserPrefs.updateGeminiApiKey(any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads profile successfully`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ProfileUiState.Success)
        assertEquals("John Doe", (state as ProfileUiState.Success).fullName)
        assertEquals("john@example.com", state.email)
        assertEquals(5, state.experienceYears)
    }

    @Test
    fun `update full name updates state`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileUiEvent.UpdateFullName("Jane Doe"))

        assertEquals("Jane Doe", (viewModel.uiState.value as ProfileUiState.Success).fullName)
    }

    @Test
    fun `save profile with blank name shows validation error`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileUiEvent.UpdateFullName(""))
        viewModel.onEvent(ProfileUiEvent.SaveProfile)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            val effect = awaitItem()
            assertTrue(effect is ProfileUiEffect.ValidationError)
            assertEquals("name", (effect as ProfileUiEffect.ValidationError).field)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save profile triggers update use case and tracks event`() = runTest {
        coEvery { mockUpdateProfile.invoke(any()) } returns Result.Success(sampleProfile)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileUiEvent.UpdateFullName("John Doe"))
        viewModel.onEvent(ProfileUiEvent.UpdateEmail("john@example.com"))
        viewModel.onEvent(ProfileUiEvent.SaveProfile)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockUpdateProfile.invoke(any()) }
        coVerify { mockTrackEvent(TrackEventRequest(eventName = "profile_save")) }
    }

    @Test
    fun `delete profile triggers delete use case and tracks event`() = runTest {
        coEvery { mockDeleteProfile.invoke() } returns Result.Success(Unit)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileUiEvent.DeleteProfile)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockDeleteProfile.invoke() }
        coVerify { mockTrackEvent(TrackEventRequest(eventName = "profile_delete")) }
    }

    @Test
    fun `api key update persists to preferences`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileUiEvent.UpdateApiKey("new-key"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockUserPrefs.updateGeminiApiKey("new-key") }
    }
}
