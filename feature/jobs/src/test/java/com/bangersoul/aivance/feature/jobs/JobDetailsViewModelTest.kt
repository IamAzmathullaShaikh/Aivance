package com.bangersoul.aivance.feature.jobs

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.job.GetJobDetailsUseCase
import com.bangersoul.aivance.core.domain.usecase.job.ToggleJobBookmarkUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
class JobDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockGetJobDetails: GetJobDetailsUseCase = mockk()
    private val mockToggleBookmark: ToggleJobBookmarkUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private val sampleJob = JobListing(
        id = "job_1",
        title = "Android Engineer",
        company = "Google",
        location = "Mountain View",
        description = "Great role",
        url = "https://google.com/jobs",
        sourceProvider = "GREENHOUSE"
    )

    private fun createViewModel(jobId: String = "job_1") = JobDetailsViewModel(
        savedStateHandle = SavedStateHandle(mapOf("jobId" to jobId)),
        getJobDetailsUseCase = mockGetJobDetails,
        toggleJobBookmarkUseCase = mockToggleBookmark,
        trackEventUseCase = mockTrackEvent
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any()) } returns Result.Success(Unit)
        coEvery { mockGetJobDetails.invoke("job_1") } returns Result.Success(sampleJob)
        coEvery { mockGetJobDetails.invoke("") } returns Result.Failure(DomainError("Job ID not provided"))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loading state on init with valid jobId`() {
        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value is JobDetailsUiState.Loading)
    }

    @Test
    fun `error state when jobId is empty`() = runTest {
        val viewModel = createViewModel(jobId = "")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is JobDetailsUiState.Error)
        assertEquals("Job ID not provided", (state as JobDetailsUiState.Error).message)
    }

    @Test
    fun `success state loads job details`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is JobDetailsUiState.Success)
        assertEquals("Android Engineer", (state as JobDetailsUiState.Success).job.title)
    }

    @Test
    fun `toggleBookmark calls toggle use case and updates state`() = runTest {
        coEvery { mockToggleBookmark.invoke("job_1") } returns Result.Success(true)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(JobDetailsUiEvent.ToggleBookmark)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockToggleBookmark.invoke("job_1") }
        val state = viewModel.uiState.value
        assertTrue(state is JobDetailsUiState.Success)
        assertEquals(true, (state as JobDetailsUiState.Success).isBookmarked)
    }

    @Test
    fun `openUrl sends external url effect and tracks event`() = runTest {
        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(JobDetailsUiEvent.OpenUrl)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            val effect = awaitItem()
            assertTrue(effect is JobDetailsUiEffect.OpenExternalUrl)
            assertEquals("https://google.com/jobs", (effect as JobDetailsUiEffect.OpenExternalUrl).url)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { mockTrackEvent(TrackEventRequest("job_details_open_url")) }
    }
}
