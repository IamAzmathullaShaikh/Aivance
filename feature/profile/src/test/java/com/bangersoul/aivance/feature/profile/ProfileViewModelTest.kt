package com.bangersoul.aivance.feature.profile

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
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

    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any()) } returns flowOf(CoreResult.Success(Unit))
        coEvery { mockLoadProfile.invoke() } returns flowOf(CoreResult.Success(null))
        coEvery { mockUserPrefs.userPreferences } returns MutableStateFlow(
            com.bangersoul.aivance.core.datastore.UserPreferences(
                geminiApiKey = "test-key"
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state loads successfully`() = runTest {
        viewModel = ProfileViewModel(
            mockLoadProfile, mockCreateProfile, mockUpdateProfile,
            mockDeleteProfile, mockUserPrefs, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        assert(viewModel.uiState.value is ProfileUiState.Success)
    }

    @Test
    fun `update profile name works`() = runTest {
        viewModel = ProfileViewModel(
            mockLoadProfile, mockCreateProfile, mockUpdateProfile,
            mockDeleteProfile, mockUserPrefs, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileUiEvent.UpdateName("John Doe"))

        assert((viewModel.uiState.value as ProfileUiState.Success).name == "John Doe")
    }

    @Test
    fun `save profile with blank name shows validation error`() = runTest {
        viewModel = ProfileViewModel(
            mockLoadProfile, mockCreateProfile, mockUpdateProfile,
            mockDeleteProfile, mockUserPrefs, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileUiEvent.UpdateName(""))
        viewModel.onEvent(ProfileUiEvent.SaveProfile)

        viewModel.effects.test {
            val effect = awaitItem()
            assert(effect is ProfileUiEffect.ValidationError)
            assert((effect as ProfileUiEffect.ValidationError).field == "name")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save profile triggers use case`() = runTest {
        coEvery { mockUpdateProfile.invoke(any()) } returns flowOf(CoreResult.Success(Unit))
        coEvery { mockCreateProfile.invoke(any()) } returns flowOf(CoreResult.Success(Unit))

        viewModel = ProfileViewModel(
            mockLoadProfile, mockCreateProfile, mockUpdateProfile,
            mockDeleteProfile, mockUserPrefs, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileUiEvent.UpdateName("John Doe"))
        viewModel.onEvent(ProfileUiEvent.UpdateEmail("john@example.com"))
        viewModel.onEvent(ProfileUiEvent.SaveProfile)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockUpdateProfile.invoke(any()) }
        coVerify { mockTrackEvent("profile_save") }
    }

    @Test
    fun `delete profile triggers use case`() = runTest {
        coEvery { mockDeleteProfile.invoke() } returns flowOf(CoreResult.Success(Unit))

        viewModel = ProfileViewModel(
            mockLoadProfile, mockCreateProfile, mockUpdateProfile,
            mockDeleteProfile, mockUserPrefs, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileUiEvent.DeleteProfile)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockDeleteProfile.invoke() }
        coVerify { mockTrackEvent("profile_delete") }
    }

    @Test
    fun `API key update persists to preferences`() = runTest {
        coEvery { mockUserPrefs.updateGeminiApiKey(any()) } returns Unit

        viewModel = ProfileViewModel(
            mockLoadProfile, mockCreateProfile, mockUpdateProfile,
            mockDeleteProfile, mockUserPrefs, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ProfileUiEvent.UpdateApiKey("new-key"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockUserPrefs.updateGeminiApiKey("new-key") }
    }
}
