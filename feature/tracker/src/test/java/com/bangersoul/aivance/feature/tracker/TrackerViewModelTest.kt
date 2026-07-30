package com.bangersoul.aivance.feature.tracker

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.feature.tracker.domain.ApplicationStatus
import com.bangersoul.aivance.feature.tracker.domain.JobApplication
import com.bangersoul.aivance.feature.tracker.domain.JobTrackerRepository
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
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TrackerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: JobTrackerRepository = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: TrackerViewModel

    private val sampleApp = JobApplication(
        company = "Google",
        role = "Android Dev",
        status = ApplicationStatus.APPLIED,
        dateApplied = Instant.now(),
        notes = "Applied via website",
        lastModified = Instant.now()
    )

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
    fun `initial state is Loading`() = runTest {
        coEvery { mockRepository.getApplications() } returns MutableStateFlow(emptyList())

        viewModel = TrackerViewModel(mockRepository, mockTrackEvent)

        viewModel.uiState.test {
            assert(awaitItem() is TrackerUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty applications show Empty state`() = runTest {
        coEvery { mockRepository.getApplications() } returns MutableStateFlow(emptyList())

        viewModel = TrackerViewModel(mockRepository, mockTrackEvent)

        viewModel.uiState.test {
            skipItems(1)
            assert(awaitItem() is TrackerUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `applications show in Success state`() = runTest {
        coEvery { mockRepository.getApplications() } returns MutableStateFlow(listOf(sampleApp))

        viewModel = TrackerViewModel(mockRepository, mockTrackEvent)

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assert(state is TrackerUiState.Success)
            assert((state as TrackerUiState.Success).applications.size == 1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filter by status works correctly`() = runTest {
        val apps = listOf(
            sampleApp.copy(company = "Google", status = ApplicationStatus.APPLIED),
            sampleApp.copy(company = "Apple", status = ApplicationStatus.INTERVIEWING)
        )
        coEvery { mockRepository.getApplications() } returns MutableStateFlow(apps)

        viewModel = TrackerViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(TrackerUiEvent.FilterByStatus(ApplicationStatus.INTERVIEWING))

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assert(state is TrackerUiState.Success)
            val success = state as TrackerUiState.Success
            assert(success.activeFilter == ApplicationStatus.INTERVIEWING)
            assert(success.applications.all { it.status == ApplicationStatus.INTERVIEWING })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clear filter shows all applications`() = runTest {
        val apps = listOf(
            sampleApp.copy(company = "Google", status = ApplicationStatus.APPLIED),
            sampleApp.copy(company = "Apple", status = ApplicationStatus.INTERVIEWING)
        )
        coEvery { mockRepository.getApplications() } returns MutableStateFlow(apps)

        viewModel = TrackerViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(TrackerUiEvent.FilterByStatus(null))

        viewModel.uiState.test {
            skipItems(1)
            val state = awaitItem()
            assert(state is TrackerUiState.Success)
            assert((state as TrackerUiState.Success).applications.size == 2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `add application with blank company shows error`() = runTest {
        coEvery { mockRepository.getApplications() } returns MutableStateFlow(emptyList())

        viewModel = TrackerViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(TrackerUiEvent.AddApplication("", "Role", ApplicationStatus.APPLIED))

        viewModel.effects.test {
            val effect = awaitItem()
            assert(effect is TrackerUiEffect.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `add valid application inserts into repository`() = runTest {
        coEvery { mockRepository.getApplications() } returns MutableStateFlow(emptyList())
        coEvery { mockRepository.addApplication(any()) } returns Unit

        viewModel = TrackerViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(TrackerUiEvent.AddApplication("Google", "Android Dev", ApplicationStatus.APPLIED))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockRepository.addApplication(any()) }
        coVerify { mockTrackEvent("tracker_add_application") }
    }

    @Test
    fun `delete application shows snackbar`() = runTest {
        val appWithId = JobApplication(
            company = "Google", role = "Dev",
            status = ApplicationStatus.APPLIED,
            dateApplied = Instant.now(),
            lastModified = Instant.now()
        )
        coEvery { mockRepository.getApplications() } returns MutableStateFlow(listOf(appWithId))
        coEvery { mockRepository.getApplicationById(any()) } returns appWithId
        coEvery { mockRepository.deleteApplication(any()) } returns Unit

        viewModel = TrackerViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(TrackerUiEvent.DeleteApplication(1L))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            val effect = awaitItem()
            assert(effect is TrackerUiEffect.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
