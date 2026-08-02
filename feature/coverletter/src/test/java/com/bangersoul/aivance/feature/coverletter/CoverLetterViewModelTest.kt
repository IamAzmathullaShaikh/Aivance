package com.bangersoul.aivance.feature.coverletter

import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.GenerateCoverLetterUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.RegenerateCoverLetterSectionUseCase
import com.bangersoul.aivance.core.util.PdfExporter
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
class CoverLetterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: CoverLetterRepository = mockk()
    private val mockGenerateUseCase: GenerateCoverLetterUseCase = mockk()
    private val mockRegenerateSectionUseCase: RegenerateCoverLetterSectionUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()
    private val mockPdfExporter: PdfExporter = mockk()

    private lateinit var viewModel: CoverLetterViewModel

    private val sampleLetter = CoverLetter(
        id = 1L,
        resumeVersionId = 1L,
        jobId = 2L,
        recruiterId = null,
        company = "Tech Corp",
        role = "Engineer"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent.invoke(any()) } returns Result.Success(Unit)
        every { mockRepository.getCoverLetters() } returns flowOf(Result.Success(listOf(sampleLetter)))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = CoverLetterViewModel(
        mockRepository,
        mockGenerateUseCase,
        mockRegenerateSectionUseCase,
        mockTrackEvent,
        mockPdfExporter
    )

    @Test
    fun `initial state is Idle`() {
        viewModel = createViewModel()
        assertTrue(viewModel.uiState.value is CoverLetterUiState.Idle)
    }

    @Test
    fun `successful generation loads letter into Success state`() = runTest(testDispatcher) {
        coEvery { mockGenerateUseCase.invoke(any()) } returns Result.Success(1L)

        viewModel = createViewModel()

        viewModel.onEvent(CoverLetterUiEvent.Generate(resumeId = 1L, versionId = 1L, jobId = 2L, recruiterId = null))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is CoverLetterUiState.Success)
        assertEquals("Tech Corp", (state as CoverLetterUiState.Success).coverLetter?.company)
    }

    @Test
    fun `generation failure shows error`() = runTest(testDispatcher) {
        coEvery { mockGenerateUseCase.invoke(any()) } returns Result.Failure(DomainError("Generation failed"))

        viewModel = createViewModel()

        viewModel.onEvent(CoverLetterUiEvent.Generate(resumeId = 1L, versionId = 1L, jobId = 2L, recruiterId = null))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is CoverLetterUiState.Error)
    }

    @Test
    fun `load with no letters stays Idle`() = runTest(testDispatcher) {
        every { mockRepository.getCoverLetters() } returns flowOf(Result.Success(emptyList()))

        viewModel = createViewModel()

        viewModel.onEvent(CoverLetterUiEvent.Load)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is CoverLetterUiState.Idle)
    }
}
