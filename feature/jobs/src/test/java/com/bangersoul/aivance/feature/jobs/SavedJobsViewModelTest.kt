package com.bangersoul.aivance.feature.jobs

import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.job.ToggleJobBookmarkUseCase
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
class SavedJobsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockJobRepository: JobRepository = mockk()
    private val mockToggleBookmark: ToggleJobBookmarkUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private val sampleJobs = listOf(
        JobListing(
            id = "1",
            title = "Android Dev",
            company = "Google",
            description = "",
            url = "",
            sourceProvider = "REMOTE_OK"
        ),
        JobListing(
            id = "2",
            title = "iOS Dev",
            company = "Apple",
            description = "",
            url = "",
            sourceProvider = "REMOTE_OK"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any()) } returns Result.Success(Unit)
        coEvery { mockToggleBookmark.invoke(any()) } returns Result.Success(true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Success with jobs`() = runTest {
        coEvery { mockJobRepository.getSavedJobs() } returns flowOf(Result.Success(sampleJobs))

        val vm = SavedJobsViewModel(mockJobRepository, mockToggleBookmark, mockTrackEvent)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is SavedJobsUiState.Success)
        assertEquals(2, (state as SavedJobsUiState.Success).jobs.size)
    }

    @Test
    fun `removeJob calls toggle bookmark use case and tracks event`() = runTest {
        coEvery { mockJobRepository.getSavedJobs() } returns flowOf(Result.Success(sampleJobs))

        val vm = SavedJobsViewModel(mockJobRepository, mockToggleBookmark, mockTrackEvent)
        testDispatcher.scheduler.advanceUntilIdle()

        vm.onEvent(SavedJobsUiEvent.RemoveJob("1"))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockToggleBookmark.invoke("1") }
        coVerify { mockTrackEvent(TrackEventRequest(eventName = "saved_jobs_remove")) }
    }

    @Test
    fun `empty state when no jobs`() = runTest {
        coEvery { mockJobRepository.getSavedJobs() } returns flowOf(Result.Success(emptyList()))

        val vm = SavedJobsViewModel(mockJobRepository, mockToggleBookmark, mockTrackEvent)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(vm.uiState.value is SavedJobsUiState.Empty)
    }

    @Test
    fun `error state when repository fails`() = runTest {
        coEvery { mockJobRepository.getSavedJobs() } returns flowOf(
            Result.Failure(com.bangersoul.aivance.core.common.result.DomainError("Load failed"))
        )

        val vm = SavedJobsViewModel(mockJobRepository, mockToggleBookmark, mockTrackEvent)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is SavedJobsUiState.Error)
        assertEquals("Load failed", (state as SavedJobsUiState.Error).message)
    }
}
