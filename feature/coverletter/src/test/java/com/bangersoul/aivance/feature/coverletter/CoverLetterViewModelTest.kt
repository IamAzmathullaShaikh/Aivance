package com.bangersoul.aivance.feature.coverletter

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ProviderError
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.ExportCoverLetterUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.GenerateCoverLetterUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.ImproveCoverLetterUseCase
import com.bangersoul.aivance.feature.coverletter.domain.model.LetterTone
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
class CoverLetterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockGenerateUseCase: GenerateCoverLetterUseCase = mockk()
    private val mockImproveUseCase: ImproveCoverLetterUseCase = mockk()
    private val mockExportUseCase: ExportCoverLetterUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: CoverLetterViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any()) } returns flowOf(Result.Success(Unit))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        viewModel = CoverLetterViewModel(mockGenerateUseCase, mockImproveUseCase, mockExportUseCase, mockTrackEvent)
        assert(viewModel.uiState.value is CoverLetterUiState.Idle)
    }

    @Test
    fun `generating with empty fields shows error`() {
        viewModel = CoverLetterViewModel(mockGenerateUseCase, mockImproveUseCase, mockExportUseCase, mockTrackEvent)
        viewModel.onEvent(CoverLetterUiEvent.Generate("", "", LetterTone.PROFESSIONAL))
        assert(viewModel.uiState.value is CoverLetterUiState.Error)
    }

    @Test
    fun `successful generation results in Success state`() = runTest {
        val content = "Dear Hiring Manager..."
        coEvery { mockGenerateUseCase.invoke(any(), any(), any()) } returns flowOf(Result.Success(content))

        viewModel = CoverLetterViewModel(mockGenerateUseCase, mockImproveUseCase, mockExportUseCase, mockTrackEvent)

        viewModel.onEvent(CoverLetterUiEvent.Generate("Resume text", "Job desc", LetterTone.PROFESSIONAL))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val generating = awaitItem()
            assert(generating is CoverLetterUiState.Generating)
            val success = awaitItem()
            assert(success is CoverLetterUiState.Success)
            assert((success as CoverLetterUiState.Success).content == content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `generation failure shows error`() = runTest {
        coEvery { mockGenerateUseCase.invoke(any(), any(), any()) } returns flowOf(
            Result.Failure(ProviderError("test", message = "API Error"))
        )

        viewModel = CoverLetterViewModel(mockGenerateUseCase, mockImproveUseCase, mockExportUseCase, mockTrackEvent)

        viewModel.onEvent(CoverLetterUiEvent.Generate("Resume text", "Job desc", LetterTone.PROFESSIONAL))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.uiState.test {
            val generating = awaitItem()
            assert(generating is CoverLetterUiState.Generating)
            val error = awaitItem()
            assert(error is CoverLetterUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `copy to clipboard triggers effect with correct content`() = runTest {
        coEvery { mockGenerateUseCase.invoke(any(), any(), any()) } returns flowOf(Result.Success("Content to copy"))

        viewModel = CoverLetterViewModel(mockGenerateUseCase, mockImproveUseCase, mockExportUseCase, mockTrackEvent)

        viewModel.onEvent(CoverLetterUiEvent.Generate("Resume", "Job", LetterTone.PROFESSIONAL))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(CoverLetterUiEvent.CopyToClipboard)

        viewModel.effects.test {
            val effect = awaitItem()
            assert(effect is CoverLetterUiEffect.CopyToClipboard)
            assert((effect as CoverLetterUiEffect.CopyToClipboard).text == "Content to copy")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reset clears state`() {
        viewModel = CoverLetterViewModel(mockGenerateUseCase, mockImproveUseCase, mockExportUseCase, mockTrackEvent)
        viewModel.onEvent(CoverLetterUiEvent.Reset)
        assert(viewModel.uiState.value is CoverLetterUiState.Idle)
    }

    @Test
    fun `tone updates correctly`() {
        viewModel = CoverLetterViewModel(mockGenerateUseCase, mockImproveUseCase, mockExportUseCase, mockTrackEvent)
        viewModel.updateTone(LetterTone.ENTHUSIASTIC)
        assert(viewModel.selectedTone.value == LetterTone.ENTHUSIASTIC)
    }
}
