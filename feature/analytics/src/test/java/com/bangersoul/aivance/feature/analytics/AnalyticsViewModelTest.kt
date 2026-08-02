package com.bangersoul.aivance.feature.analytics

import com.bangersoul.aivance.core.common.model.AnalyticsSnapshot
import com.bangersoul.aivance.core.common.model.CareerRecommendation
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import io.mockk.coEvery
import io.mockk.every
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
class AnalyticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: AnalyticsRepository = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private val snapshot = AnalyticsSnapshot(
        id = 1L,
        careerScore = 72,
        kpis = mapOf("applications" to 5.0)
    )
    private val recommendation = CareerRecommendation(
        id = 1L,
        title = "Improve resume",
        description = "Add more keywords",
        priority = "HIGH",
        category = "RESUME"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent.invoke(any()) } returns Result.Success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = AnalyticsViewModel(mockRepository, mockTrackEvent)

    @Test
    fun `success loads snapshot and recommendations`() = runTest(testDispatcher) {
        every { mockRepository.getSnapshots() } returns flowOf(Result.Success(listOf(snapshot)))
        every { mockRepository.getActiveRecommendations() } returns flowOf(Result.Success(listOf(recommendation)))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AnalyticsUiState.Success)
        val success = state as AnalyticsUiState.Success
        assertEquals(72, success.latestSnapshot?.careerScore)
        assertEquals(1, success.recommendations.size)
        assertEquals(1, success.historicalSnapshots.size)
    }

    @Test
    fun `repository failure surfaces error`() = runTest(testDispatcher) {
        every { mockRepository.getSnapshots() } returns flowOf(
            Result.Failure(com.bangersoul.aivance.core.common.result.DomainError("db down"))
        )
        every { mockRepository.getActiveRecommendations() } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AnalyticsUiState.Error)
    }

    @Test
    fun `refresh reloads from loading`() = runTest(testDispatcher) {
        every { mockRepository.getSnapshots() } returns flowOf(Result.Success(emptyList()))
        every { mockRepository.getActiveRecommendations() } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.refresh()
        assertEquals(AnalyticsUiState.Loading, viewModel.uiState.value)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AnalyticsUiState.Success)
    }

    @Test
    fun `dismissRecommendation calls repository`() = runTest(testDispatcher) {
        every { mockRepository.getSnapshots() } returns flowOf(Result.Success(emptyList()))
        every { mockRepository.getActiveRecommendations() } returns flowOf(Result.Success(emptyList()))
        coEvery { mockRepository.dismissRecommendation(any()) } returns Result.Success(Unit)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.dismissRecommendation(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        coEvery { mockRepository.dismissRecommendation(1L) }
    }
}
