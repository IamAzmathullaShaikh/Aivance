package com.bangersoul.aivance.feature.jobs

import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.job.RemoveSavedJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SearchSavedJobsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
    private val mockSearchSavedJobs: SearchSavedJobsUseCase = mockk()
    private val mockRemoveSavedJob: RemoveSavedJobUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private val sampleJobs = listOf(
        JobListing("1", "Android Dev", "Google", description = "", url = "", sourceProvider = ""),
        JobListing("2", "iOS Dev", "Apple", description = "", url = "", sourceProvider = "")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any<TrackEventRequest>()) } returns Result.Success(Unit)
        coEvery { mockSearchSavedJobs(any()) } returns flowOf(Result.Success(sampleJobs))
        coEvery { mockRemoveSavedJob(any()) } returns flowOf(Result.Success(Unit))
    }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `initial state is Success with jobs`() = runTest {
        val vm = SavedJobsViewModel(mockSearchSavedJobs, mockRemoveSavedJob, mockTrackEvent)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state is SavedJobsUiState.Success)
        assertEquals(2, (state as SavedJobsUiState.Success).jobs.size)
    }

    @Test
    fun `removeJob calls remove use case`() = runTest {
        val vm = SavedJobsViewModel(mockSearchSavedJobs, mockRemoveSavedJob, mockTrackEvent)
        advanceUntilIdle()
        vm.onEvent(SavedJobsUiEvent.RemoveJob("1"))
        advanceUntilIdle()
        coVerify { mockRemoveSavedJob("1") }
    }

    @Test
    fun `empty state when no jobs`() = runTest {
        coEvery { mockSearchSavedJobs(any()) } returns flowOf(Result.Success(emptyList()))
        val vm = SavedJobsViewModel(mockSearchSavedJobs, mockRemoveSavedJob, mockTrackEvent)
        advanceUntilIdle()
        assertTrue(vm.uiState.value is SavedJobsUiState.Empty)
    }
}
