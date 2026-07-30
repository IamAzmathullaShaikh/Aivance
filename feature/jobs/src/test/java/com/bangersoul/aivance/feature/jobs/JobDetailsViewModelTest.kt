package com.bangersoul.aivance.feature.jobs

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.job.ApplyToJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.BookmarkJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.GetJobDetailsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JobDetailsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockGetJobDetails: GetJobDetailsUseCase = mockk()
    private val mockBookmarkJob: BookmarkJobUseCase = mockk()
    private val mockApplyToJob: ApplyToJobUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private val sampleJob = JobListing(
        id = "job_1", title = "Android Engineer", company = "Google",
        location = "Mountain View", description = "Great role", url = "https://google.com/jobs",
        sourceProvider = "GREENHOUSE"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any<TrackEventRequest>()) } returns Result.Success(Unit)
        coEvery { mockGetJobDetails("job_1") } returns flowOf(Result.Success(sampleJob))
        coEvery { mockGetJobDetails("") } returns flowOf(Result.Failure(DomainError("Job ID not provided")))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loading state on init with valid jobId`() {
        val savedStateHandle = SavedStateHandle(mapOf("jobId" to "job_1"))
        coEvery { mockBookmarkJob(any()) } returns flowOf(Result.Success(false))

        val viewModel = JobDetailsViewModel(
            savedStateHandle = savedStateHandle,
            getJobDetailsUseCase = mockGetJobDetails,
            bookmarkJobUseCase = mockBookmarkJob,
            applyToJobUseCase = mockApplyToJob,
            trackEventUseCase = mockTrackEvent
        )

        assertTrue(viewModel.uiState.value is JobDetailsUiState.Loading)
    }

    @Test
    fun `error state when jobId is empty`() {
        val savedStateHandle = SavedStateHandle(mapOf("jobId" to ""))
        coEvery { mockBookmarkJob(any()) } returns flowOf(Result.Success(false))

        val viewModel = JobDetailsViewModel(
            savedStateHandle = savedStateHandle,
            getJobDetailsUseCase = mockGetJobDetails,
            bookmarkJobUseCase = mockBookmarkJob,
            applyToJobUseCase = mockApplyToJob,
            trackEventUseCase = mockTrackEvent
        )

        assertTrue(viewModel.uiState.value is JobDetailsUiState.Error)
        val error = viewModel.uiState.value as JobDetailsUiState.Error
        assertEquals("Job ID not provided", error.message)
    }

    @Test
    fun `success state loads job details`() = runTest {
        coEvery { mockBookmarkJob(any()) } returns flowOf(Result.Success(false))
        val savedStateHandle = SavedStateHandle(mapOf("jobId" to "job_1"))

        val viewModel = JobDetailsViewModel(
            savedStateHandle = savedStateHandle,
            getJobDetailsUseCase = mockGetJobDetails,
            bookmarkJobUseCase = mockBookmarkJob,
            applyToJobUseCase = mockApplyToJob,
            trackEventUseCase = mockTrackEvent
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is JobDetailsUiState.Success)
        val success = state as JobDetailsUiState.Success
        assertEquals("Android Engineer", success.job.title)
    }

    @Test
    fun `toggleBookmark calls bookmark use case`() = runTest {
        coEvery { mockBookmarkJob("job_1") } returns flowOf(Result.Success(true))
        val savedStateHandle = SavedStateHandle(mapOf("jobId" to "job_1"))

        val viewModel = JobDetailsViewModel(
            savedStateHandle = savedStateHandle,
            getJobDetailsUseCase = mockGetJobDetails,
            bookmarkJobUseCase = mockBookmarkJob,
            applyToJobUseCase = mockApplyToJob,
            trackEventUseCase = mockTrackEvent
        )
        advanceUntilIdle()

        viewModel.onEvent(JobDetailsUiEvent.ToggleBookmark)
        advanceUntilIdle()

        coVerify { mockBookmarkJob("job_1") }
    }

    @Test
    fun `apply calls apply use case`() = runTest {
        coEvery { mockBookmarkJob("job_1") } returns flowOf(Result.Success(false))
        coEvery { mockApplyToJob("job_1") } returns flowOf(Result.Success(Unit))
        val savedStateHandle = SavedStateHandle(mapOf("jobId" to "job_1"))

        val viewModel = JobDetailsViewModel(
            savedStateHandle = savedStateHandle,
            getJobDetailsUseCase = mockGetJobDetails,
            bookmarkJobUseCase = mockBookmarkJob,
            applyToJobUseCase = mockApplyToJob,
            trackEventUseCase = mockTrackEvent
        )
        advanceUntilIdle()

        viewModel.onEvent(JobDetailsUiEvent.Apply)
        advanceUntilIdle()

        coVerify { mockApplyToJob("job_1") }
    }

    @Test
    fun `openUrl sends effect`() = runTest {
        coEvery { mockBookmarkJob("job_1") } returns flowOf(Result.Success(false))
        val savedStateHandle = SavedStateHandle(mapOf("jobId" to "job_1"))

        val viewModel = JobDetailsViewModel(
            savedStateHandle = savedStateHandle,
            getJobDetailsUseCase = mockGetJobDetails,
            bookmarkJobUseCase = mockBookmarkJob,
            applyToJobUseCase = mockApplyToJob,
            trackEventUseCase = mockTrackEvent
        )
        advanceUntilIdle()

        viewModel.onEvent(JobDetailsUiEvent.OpenUrl)
        val effect = viewModel.effects.firstOrNull()
        assertTrue(effect is JobDetailsUiEffect.OpenExternalUrl)
        assertEquals("https://google.com/jobs", (effect as JobDetailsUiEffect.OpenExternalUrl).url)
    }
}
