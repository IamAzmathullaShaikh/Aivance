package com.bangersoul.aivance.feature.jobs

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.ProviderError
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.job.ApplyToJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.BookmarkJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.GetJobDetailsUseCase
import com.bangersoul.aivance.core.domain.usecase.job.RemoveSavedJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SaveJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SearchRemoteJobsUseCase
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
class JobsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockSearchJobs: SearchJobsUseCase = mockk()
    private val mockSearchRemote: SearchRemoteJobsUseCase = mockk()
    private val mockGetDetails: GetJobDetailsUseCase = mockk()
    private val mockSaveJob: SaveJobUseCase = mockk()
    private val mockBookmark: BookmarkJobUseCase = mockk()
    private val mockRemoveSaved: RemoveSavedJobUseCase = mockk()
    private val mockApply: ApplyToJobUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: JobsViewModel

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
    fun `search returns jobs successfully`() = runTest {
        val jobs = listOf(
            JobListing(id = "1", title = "Android Dev", company = "Google", url = "https://careers.google.com", sourceProvider = "REMOTE_OK"),
            JobListing(id = "2", title = "iOS Dev", company = "Apple", url = "https://apple.com", sourceProvider = "REMOTE_OK")
        )
        coEvery { mockSearchJobs.invoke(any()) } returns flowOf(CoreResult.Success(jobs))

        viewModel = JobsViewModel(
            mockSearchJobs, mockSearchRemote, mockGetDetails, mockSaveJob,
            mockBookmark, mockRemoveSaved, mockApply, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is JobsUiState.Success)
            assert((state as JobsUiState.Success).jobs.size == 2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty search results show Empty state`() = runTest {
        coEvery { mockSearchJobs.invoke(any()) } returns flowOf(CoreResult.Success(emptyList()))

        viewModel = JobsViewModel(
            mockSearchJobs, mockSearchRemote, mockGetDetails, mockSaveJob,
            mockBookmark, mockRemoveSaved, mockApply, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is JobsUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search failure shows error state`() = runTest {
        coEvery { mockSearchJobs.invoke(any()) } returns flowOf(
            CoreResult.Failure(ProviderError("test", message = "Search failed"))
        )

        viewModel = JobsViewModel(
            mockSearchJobs, mockSearchRemote, mockGetDetails, mockSaveJob,
            mockBookmark, mockRemoveSaved, mockApply, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assert(state is JobsUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `view details triggers navigation effect`() = runTest {
        coEvery { mockSearchJobs.invoke(any()) } returns flowOf(CoreResult.Success(emptyList()))

        viewModel = JobsViewModel(
            mockSearchJobs, mockSearchRemote, mockGetDetails, mockSaveJob,
            mockBookmark, mockRemoveSaved, mockApply, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(JobsUiEvent.ViewDetails("job_1"))

        viewModel.effects.test {
            val effect = awaitItem()
            assert(effect is JobsUiEffect.NavigateToDetails)
            assert((effect as JobsUiEffect.NavigateToDetails).jobId == "job_1")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `open application triggers external url effect`() = runTest {
        coEvery { mockSearchJobs.invoke(any()) } returns flowOf(CoreResult.Success(emptyList()))

        viewModel = JobsViewModel(
            mockSearchJobs, mockSearchRemote, mockGetDetails, mockSaveJob,
            mockBookmark, mockRemoveSaved, mockApply, mockTrackEvent
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(JobsUiEvent.OpenApplication("https://example.com/job"))

        viewModel.effects.test {
            val effect = awaitItem()
            assert(effect is JobsUiEffect.OpenExternalUrl)
            assert((effect as JobsUiEffect.OpenExternalUrl).url == "https://example.com/job")
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { mockTrackEvent("job_open_application") }
    }
}
