package com.bangersoul.aivance.feature.ats

import com.bangersoul.aivance.core.common.model.AtsReport
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.ats.AnalyzeJobDescriptionUseCase
import com.bangersoul.aivance.core.domain.usecase.ats.PerformAtsAnalysisUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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
class AtsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockResumeRepository: ResumeRepository = mockk()
    private val mockAnalyzeJd: AnalyzeJobDescriptionUseCase = mockk()
    private val mockPerformAts: PerformAtsAnalysisUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private lateinit var viewModel: AtsViewModel

    private val sampleResume = Resume(id = 1L, name = "resume.pdf", fileName = "resume.pdf", rawText = "text")
    private val sampleVersion = ResumeVersion(id = 1L, resumeId = 1L, versionName = "Original Import")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent.invoke(any()) } returns Result.Success(Unit)
        every { mockResumeRepository.getResumes() } returns flowOf(Result.Success(listOf(sampleResume)))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        AtsViewModel(mockResumeRepository, mockAnalyzeJd, mockPerformAts, mockTrackEvent)

    @Test
    fun `initial state is SelectingResume`() {
        viewModel = createViewModel()
        assertTrue(viewModel.uiState.value is AtsUiState.SelectingResume)
    }

    @Test
    fun `selecting resume version transitions to InputJobDescription`() {
        viewModel = createViewModel()
        viewModel.onEvent(AtsUiEvent.SelectResumeVersion(sampleResume, sampleVersion))
        assertTrue(viewModel.uiState.value is AtsUiState.InputJobDescription)
    }

    // STEP 9 live-reactive scoring only fires once the JD exceeds 50 characters
    // (the combine → debounce(800) pipeline is gated on jd.length > 50).
    private val longJd = "Senior Android Engineer with 5+ years of experience building scalable " +
        "mobile applications using Kotlin, Jetpack Compose, and Clean Architecture, with strong " +
        "knowledge of coroutines, Hilt, Room, and CI/CD pipelines."

    @Test
    fun `analyze success transitions to DisplayReport`() = runTest(testDispatcher) {
        coEvery { mockAnalyzeJd.invoke(any()) } returns Result.Success(99L)
        coEvery { mockPerformAts.invoke(any()) } returns Result.Success(
            AtsReport(resumeVersionId = 1L, jobDescriptionId = 99L, overallScore = 85, matchPercentage = 80)
        )

        viewModel = createViewModel()
        viewModel.onEvent(AtsUiEvent.SelectResumeVersion(sampleResume, sampleVersion))
        viewModel.onEvent(AtsUiEvent.Analyze(longJd))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AtsUiState.DisplayReport)
        assertEquals(85, (state as AtsUiState.DisplayReport).report.overallScore)
        coVerify { mockTrackEvent.invoke(match { it.eventName == "ats_analyze_success" }) }
    }

    @Test
    fun `analyze failure transitions to Error`() = runTest(testDispatcher) {
        coEvery { mockAnalyzeJd.invoke(any()) } returns Result.Failure(DomainError("AI parsing failed"))

        viewModel = createViewModel()
        viewModel.onEvent(AtsUiEvent.SelectResumeVersion(sampleResume, sampleVersion))
        viewModel.onEvent(AtsUiEvent.Analyze(longJd))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is AtsUiState.Error)
    }

    @Test
    fun `reset returns to SelectingResume`() {
        viewModel = createViewModel()
        viewModel.onEvent(AtsUiEvent.SelectResumeVersion(sampleResume, sampleVersion))
        viewModel.onEvent(AtsUiEvent.Reset)
        assertTrue(viewModel.uiState.value is AtsUiState.SelectingResume)
    }
}
