package com.bangersoul.aivance.feature.ats

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.feature.ats.domain.AtsRepository
import com.bangersoul.aivance.feature.ats.domain.AtsResult
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

@OptIn(ExperimentalCoroutinesApi::class)
class AtsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: AtsRepository = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: AtsViewModel

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
        coEvery { mockRepository.getAtsResults() } returns MutableStateFlow(emptyList())

        viewModel = AtsViewModel(mockRepository, mockTrackEvent)

        viewModel.uiState.test {
            val item = awaitItem()
            assert(item is AtsUiState.Loading) { "Expected Loading but got $item" }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `empty results emit Empty state`() = runTest {
        coEvery { mockRepository.getAtsResults() } returns MutableStateFlow(emptyList())

        viewModel = AtsViewModel(mockRepository, mockTrackEvent)

        viewModel.uiState.test {
            skipItems(1) // skip Loading
            assert(awaitItem() is AtsUiState.Empty)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `results emit Success state with latest result and history`() = runTest {
        val results = listOf(
            AtsResult(id = 1, score = 85, resumeName = "Resume1", date = 1000L),
            AtsResult(id = 2, score = 92, resumeName = "Resume2", date = 2000L)
        )
        coEvery { mockRepository.getAtsResults() } returns MutableStateFlow(results)

        viewModel = AtsViewModel(mockRepository, mockTrackEvent)

        viewModel.uiState.test {
            skipItems(1) // skip Loading
            val state = awaitItem()
            assert(state is AtsUiState.Success)
            val success = state as AtsUiState.Success
            assert(success.latestResult?.id == 2L) { "Latest should be most recent" }
            assert(success.history.size == 1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search filters results correctly`() = runTest {
        val results = listOf(
            AtsResult(id = 1, score = 85, resumeName = "Android Dev", missingKeywords = listOf("Kotlin")),
            AtsResult(id = 2, score = 70, resumeName = "iOS Dev", missingKeywords = listOf("Swift"))
        )
        coEvery { mockRepository.getAtsResults() } returns MutableStateFlow(results)

        viewModel = AtsViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(AtsUiEvent.Search("Android"))

        viewModel.uiState.test {
            skipItems(1) // skip Loading
            val state = awaitItem()
            assert(state is AtsUiState.Success)
            val success = state as AtsUiState.Success
            assert(success.searchQuery == "Android")
            assert(success.filteredHistory.size == 1)
            assert(success.filteredHistory.first().resumeName == "Android Dev")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `delete result triggers repository and shows snackbar`() = runTest {
        val results = listOf(
            AtsResult(id = 1, score = 85, resumeName = "Resume1", date = 1000L),
            AtsResult(id = 2, score = 92, resumeName = "Resume2", date = 2000L)
        )
        coEvery { mockRepository.getAtsResults() } returns MutableStateFlow(results)
        coEvery { mockRepository.deleteAtsResult(any()) } returns Unit
        coEvery { mockRepository.saveAtsResult(any()) } returns 1L

        viewModel = AtsViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(AtsUiEvent.DeleteResult(2L))

        viewModel.effects.test {
            val effect = awaitItem()
            assert(effect is AtsUiEffect.ShowSnackbar)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { mockRepository.deleteAtsResult(2L) }
    }

    @Test
    fun `refresh triggers track event`() = runTest {
        coEvery { mockRepository.getAtsResults() } returns MutableStateFlow(emptyList())

        viewModel = AtsViewModel(mockRepository, mockTrackEvent)

        viewModel.onEvent(AtsUiEvent.Refresh)

        coVerify { mockTrackEvent("ats_refresh") }
    }
}
