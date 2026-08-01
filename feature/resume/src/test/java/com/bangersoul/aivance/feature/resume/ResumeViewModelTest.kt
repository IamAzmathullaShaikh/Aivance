package com.bangersoul.aivance.feature.resume

import android.net.Uri
import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.AtsResult
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeAnalysis
import com.bangersoul.aivance.core.common.model.ResumeSection
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.AnalyseResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.AtsScoreResponse
import com.bangersoul.aivance.core.domain.usecase.resume.CalculateATSScoreUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ExportFormat
import com.bangersoul.aivance.core.domain.usecase.resume.ExportResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.GenerateResumeSummaryUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ImportResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ImproveResumeUseCase
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
class ResumeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: ResumeRepository = mockk()
    private val mockAnalyse: AnalyseResumeUseCase = mockk()
    private val mockCalculateAts: CalculateATSScoreUseCase = mockk()
    private val mockImprove: ImproveResumeUseCase = mockk()
    private val mockGenerateSummary: GenerateResumeSummaryUseCase = mockk()
    private val mockExport: ExportResumeUseCase = mockk()
    private val mockImport: ImportResumeUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()

    private val section = ResumeSection(
        id = 1L,
        versionId = 1L,
        sectionType = "EXPERIENCE",
        title = "Experience",
        content = "Android Engineer at Acme"
    )
    private val version = ResumeVersion(
        id = 1L,
        resumeId = 1L,
        versionName = "v1",
        sections = listOf(section)
    )
    private val resume = Resume(id = 1L, name = "resume.pdf", primaryVersionId = 1L)

    private fun createViewModel() = ResumeViewModel(
        mockRepository, mockAnalyse, mockCalculateAts, mockImprove,
        mockGenerateSummary, mockExport, mockImport, mockTrackEvent
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any()) } returns Result.Success(Unit)
        coEvery { mockRepository.getResumes() } returns flowOf(Result.Success(listOf(resume)))
        coEvery { mockRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(version)))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty resume library shows Idle state after refresh`() = runTest {
        coEvery { mockRepository.getResumes() } returns flowOf(Result.Success(emptyList()))

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeUiEvent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ResumeUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun `load resumes populates versions and selected version`() = runTest {
        val viewModel = createViewModel()
        viewModel.onEvent(ResumeUiEvent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ResumeUiState.Success)
        assertEquals(1, (state as ResumeUiState.Success).versions.size)
        assertEquals("v1", state.selectedVersion?.versionName)
    }

    @Test
    fun `analyze updates ats score and analysis result`() = runTest {
        val analysis = ResumeAnalysis(
            overallScore = 80,
            matchingKeywords = listOf("Kotlin"),
            missingKeywords = listOf("Rust"),
            suggestions = listOf("Add Rust"),
            matchSummary = "Good match"
        )
        val atsResult = AtsResult(
            score = 80,
            resumeName = "resume.pdf",
            feedback = "Good match"
        )
        coEvery { mockCalculateAts.invoke(any()) } returns Result.Success(
            AtsScoreResponse(atsResult = atsResult, analysis = analysis)
        )

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeUiEvent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ResumeUiEvent.Analyze("Android Developer"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ResumeUiState.Success)
        assertEquals(80, (state as ResumeUiState.Success).atsScore)
        assertEquals(2, state.analysisResult?.keywords?.size)
    }

    @Test
    fun `import file tracks event and reloads`() = runTest {
        coEvery { mockImport.invoke(any()) } returns Result.Success(2L)

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ResumeUiEvent.ImportFile(mockk<Uri>()))
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { mockTrackEvent(TrackEventRequest("resume_import")) }
        coVerify { mockImport.invoke(any()) }
    }

    @Test
    fun `export sends export result effect`() = runTest {
        coEvery { mockExport.invoke(any()) } returns Result.Success("/tmp/resume.txt")

        val viewModel = createViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ResumeUiEvent.Export(ExportFormat.TXT))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.effects.test {
            val effect = awaitItem()
            assertTrue(effect is ResumeUiEffect.ExportResult)
            assertEquals("/tmp/resume.txt", (effect as ResumeUiEffect.ExportResult).path)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error when repository fails to load`() = runTest {
        coEvery { mockRepository.getResumes() } returns flowOf(
            Result.Failure(com.bangersoul.aivance.core.common.result.DomainError("Failed"))
        )

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeUiEvent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ResumeUiState.Error)
    }
}
