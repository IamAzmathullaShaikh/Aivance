package com.bangersoul.aivance.feature.dashboard

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.feature.dashboard.data.DashboardRepositoryImpl
import com.bangersoul.aivance.feature.dashboard.domain.DashboardData
import com.bangersoul.aivance.feature.dashboard.domain.ResumeStatus
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: DashboardRepositoryImpl = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: DashboardViewModel

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
    fun `initial state becomes Loading`() = runTest {
        val dataFlow = MutableStateFlow(
            DashboardData(
                profileCompletion = 75,
                resumeStatus = ResumeStatus(fileName = "resume.pdf", uploadedDate = LocalDate.now()),
                atsScore = 85,
                activeApplications = 5,
                interviewPrepStatus = "Ready"
            )
        )
        coEvery { mockRepository.getDashboardData() } returns dataFlow

        viewModel = DashboardViewModel(mockRepository, mockTrackEvent)

        viewModel.uiState.test {
            val item = awaitItem()
            assert(item is DashboardUiState.Loading) { "Expected Loading but got $item" }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `repository emits data then state becomes Success`() = runTest {
        val data = DashboardData(
            profileCompletion = 75,
            resumeStatus = ResumeStatus(fileName = "resume.pdf", uploadedDate = LocalDate.now()),
            atsScore = 85,
            activeApplications = 5,
            interviewPrepStatus = "Scheduled"
        )
        coEvery { mockRepository.getDashboardData() } returns MutableStateFlow(data)

        viewModel = DashboardViewModel(mockRepository, mockTrackEvent)

        viewModel.uiState.test {
            skipItems(1) // skip initial Loading
            val successState = awaitItem()
            assert(successState is DashboardUiState.Success)
            val success = successState as DashboardUiState.Success
            assert(success.profileCompletion == 75)
            assert(success.atsScore == 85)
            assert(success.activeApplications == 5)
            assert(success.interviewPrepStatus == "Scheduled")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigation events send correct effects`() = runTest {
        coEvery { mockRepository.getDashboardData() } returns MutableStateFlow(
            DashboardData(
                profileCompletion = 50,
                resumeStatus = ResumeStatus("resume.pdf", LocalDate.now()),
                atsScore = 90,
                activeApplications = 3,
                interviewPrepStatus = "Ready"
            )
        )

        viewModel = DashboardViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(DashboardUiEvent.NavigateToResume)

        viewModel.effects.test {
            val effect = awaitItem()
            assert(effect is DashboardUiEffect.NavigateTo)
            assert((effect as DashboardUiEffect.NavigateTo).route == "resume")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh triggers track event`() = runTest {
        coEvery { mockRepository.getDashboardData() } returns MutableStateFlow(
            DashboardData(
                profileCompletion = 50,
                resumeStatus = ResumeStatus("resume.pdf", LocalDate.now()),
                atsScore = 70,
                activeApplications = 1,
                interviewPrepStatus = "Ready"
            )
        )

        viewModel = DashboardViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(DashboardUiEvent.Refresh)

        coVerify { mockTrackEvent("dashboard_refresh") }
    }

    @Test
    fun `settings event opens settings`() = runTest {
        coEvery { mockRepository.getDashboardData() } returns MutableStateFlow(
            DashboardData(
                profileCompletion = 60,
                resumeStatus = ResumeStatus("resume.pdf", LocalDate.now()),
                atsScore = 75,
                activeApplications = 4,
                interviewPrepStatus = "Ready"
            )
        )

        viewModel = DashboardViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(DashboardUiEvent.NavigateToSettings)

        viewModel.effects.test {
            assert(awaitItem() is DashboardUiEffect.OpenSettings)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
