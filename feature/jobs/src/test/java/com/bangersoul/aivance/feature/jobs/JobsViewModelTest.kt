package com.bangersoul.aivance.feature.jobs

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsRequest
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsUseCase
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
class JobsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockSearchJobs: SearchJobsUseCase = mockk()
    private val mockToggleBookmark: ToggleJobBookmarkUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private val jobs = listOf(
        JobListing(
            id = "1",
            title = "Android Dev",
            company = "Google",
            description = "Android role",
            url = "https://careers.google.com",
            sourceProvider = "REMOTE_OK"
        ),
        JobListing(
            id = "2",
            title = "iOS Dev",
            company = "Apple",
            description = "iOS role",
            url = "https://apple.com",
            sourceProvider = "REMOTE_OK"
        )
    )

    private fun createViewModel() = JobsViewModel(
        mockSearchJobs, mockToggleBookmark, mockTrackEvent
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any()) } returns Result.Success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `search returns jobs successfully`() = runTest {
        coEvery { mockSearchJobs.invoke(any()) } returns Result.Success(jobs)

        val viewModel = createViewModel()
        viewModel.onEvent(JobsUiEvent.Search("Android"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is JobsUiState.Success)
        assertEquals(2, (state as JobsUiState.Success).jobs.size)
        assertEquals("Android", state.filter.query)
    }

    @Test
    fun `search tracks discovery event`() = runTest {
        coEvery { mockSearchJobs.invoke(any()) } returns Result.Success(emptyList())

        val viewModel = createViewModel()
        viewModel.onEvent(JobsUiEvent.Search("Android"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockTrackEvent(TrackEventRequest("job_discovery_search")) }
    }

    @Test
    fun `empty search results show Success with empty jobs`() = runTest {
        coEvery { mockSearchJobs.invoke(any()) } returns Result.Success(emptyList())

        val viewModel = createViewModel()
        viewModel.onEvent(JobsUiEvent.Search("nothing"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is JobsUiState.Success)
        assertEquals(0, (state as JobsUiState.Success).jobs.size)
    }

    @Test
    fun `search failure shows error state`() = runTest {
        coEvery { mockSearchJobs.invoke(any()) } returns Result.Failure(
            com.bangersoul.aivance.core.common.result.ProviderError("test", message = "Search failed")
        )

        val viewModel = createViewModel()
        viewModel.onEvent(JobsUiEvent.Search("Android"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is JobsUiState.Error)
        assertEquals("Search failed", (state as JobsUiState.Error).message)
    }

    @Test
    fun `view details triggers navigation effect`() = runTest {
        coEvery { mockSearchJobs.invoke(any()) } returns Result.Success(emptyList())

        val viewModel = createViewModel()
        viewModel.onEvent(JobsUiEvent.ViewDetails("job_1"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            val effect = awaitItem()
            assertTrue(effect is JobsUiEffect.NavigateToDetails)
            assertEquals("job_1", (effect as JobsUiEffect.NavigateToDetails).jobId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle bookmark shows snackbar effect`() = runTest {
        coEvery { mockToggleBookmark.invoke("job_1") } returns Result.Success(true)

        val viewModel = createViewModel()
        viewModel.onEvent(JobsUiEvent.ToggleBookmark("job_1"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            val effect = awaitItem()
            assertTrue(effect is JobsUiEffect.ShowSnackbar)
            assertEquals("Job bookmarked", (effect as JobsUiEffect.ShowSnackbar).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `update filter triggers search with new filter`() = runTest {
        coEvery { mockSearchJobs.invoke(any()) } returns Result.Success(jobs)

        val viewModel = createViewModel()
        val newFilter = JobSearchFilter(query = "Kotlin")
        viewModel.onEvent(JobsUiEvent.UpdateFilter(newFilter))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockSearchJobs.invoke(SearchJobsRequest(filter = newFilter)) }
        val state = viewModel.uiState.value
        assertTrue(state is JobsUiState.Success)
        assertEquals("Kotlin", (state as JobsUiState.Success).filter.query)
    }
}
