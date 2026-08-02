package com.bangersoul.aivance.feature.dashboard

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.user.LoadProfileUseCase
import com.bangersoul.aivance.feature.dashboard.domain.DashboardData
import com.bangersoul.aivance.feature.dashboard.domain.DashboardRepository
import com.bangersoul.aivance.feature.dashboard.domain.JobRecommendation
import com.bangersoul.aivance.feature.dashboard.domain.RecentActivity
import com.bangersoul.aivance.feature.dashboard.domain.ResumeStatus
import com.bangersoul.aivance.feature.dashboard.domain.UpcomingInterview
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockDashboardRepository: DashboardRepository = mockk()
    private val mockJobRepository: JobRepository = mockk()
    private val mockLoadProfile: LoadProfileUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: DashboardViewModel

    private fun sampleData() = DashboardData(
        profileCompletion = 75,
        resumeStatus = ResumeStatus(fileName = "resume.pdf", uploadedDate = LocalDate.now()),
        atsScore = 85,
        activeApplications = 5,
        interviewPrepStatus = "Scheduled",
        careerScore = 78,
        upcomingInterviews = listOf(
            UpcomingInterview(id = "1", company = "Google", role = "Android Engineer", dateTime = "Fri 10:00")
        ),
        jobRecommendations = listOf(
            JobRecommendation(id = "1", title = "Senior Android Engineer", company = "Acme")
        ),
        recentActivity = listOf(
            RecentActivity(id = "1", description = "Applied to Acme", date = LocalDate.now())
        )
    )

    private fun job(id: String) = JobListing(
        id = id,
        title = "Android Engineer",
        company = "Acme",
        description = "desc",
        url = "https://acme.com/jobs",
        sourceProvider = "test"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent.invoke(any()) } returns Result.Success(Unit)
        every { mockDashboardRepository.getDashboardData() } returns MutableStateFlow(sampleData())
        every { mockJobRepository.getSavedJobs() } returns MutableStateFlow(
            Result.Success(listOf(job("1"), job("2"), job("3")))
        )
        every { mockLoadProfile.invoke() } returns MutableStateFlow(
            Result.Success(
                UserProfile(
                    fullName = "Azmath Shaik",
                    email = "azmath@aivance.com",
                    targetRole = "Software Engineer"
                )
            )
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = DashboardViewModel(
        mockDashboardRepository,
        mockJobRepository,
        mockLoadProfile,
        mockTrackEvent
    )

    @Test
    fun `initial state is loading`() {
        viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `aggregates real data into career HQ state`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.uiState.test {
            // Skip the initial Loading emission
            skipItems(1)
            val state = awaitItem()

            assertTrue(!state.isLoading)
            assertEquals(78, state.careerScore)
            assertEquals(85, state.atsScore)
            assertEquals(5, state.activeApplications)
            assertEquals(3, state.savedJobs)
            assertEquals("Fri 10:00", state.nextInterview)
            assertEquals("Software Engineer", state.userDesignation)
            assertTrue(state.greeting.contains("Azmath"))
            assertEquals(1, state.recentActivity.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `navigation events send correct effects`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.onEvent(DashboardUiEvent.NavigateToResume)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            val effect = awaitItem()
            assertTrue(effect is DashboardUiEffect.NavigateTo)
            assertEquals("resume", (effect as DashboardUiEffect.NavigateTo).route)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh triggers track event`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.onEvent(DashboardUiEvent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockTrackEvent.invoke(match { it.eventName == "dashboard_refresh" }) }
    }

    @Test
    fun `settings event opens settings`() = runTest(testDispatcher) {
        viewModel = createViewModel()

        viewModel.onEvent(DashboardUiEvent.NavigateToSettings)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            assertTrue(awaitItem() is DashboardUiEffect.OpenSettings)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
