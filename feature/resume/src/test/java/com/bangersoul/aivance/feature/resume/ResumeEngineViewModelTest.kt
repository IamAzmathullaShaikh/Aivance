package com.bangersoul.aivance.feature.resume

import android.net.Uri
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeSection
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.AtsScoreResponse
import com.bangersoul.aivance.core.domain.usecase.resume.CalculateATSScoreUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ExportResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ImportResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ImproveResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ParseResumeUseCase
import com.bangersoul.aivance.core.common.model.AtsResult
import com.bangersoul.aivance.core.common.model.ResumeAnalysis
import com.bangersoul.aivance.core.util.PdfExporter
import io.mockk.coEvery
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
class ResumeEngineViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepository: ResumeRepository = mockk()
    private val mockImport: ImportResumeUseCase = mockk()
    private val mockParse: ParseResumeUseCase = mockk()
    private val mockCalculateAts: CalculateATSScoreUseCase = mockk()
    private val mockImprove: ImproveResumeUseCase = mockk()
    private val mockExport: ExportResumeUseCase = mockk()
    private val mockTrackEvent: TrackEventUseCase = mockk()
    private val mockPdfExporter: PdfExporter = mockk()
    private val mockDocxExporter: com.bangersoul.aivance.core.util.DocxExporter = mockk()

    private val section = ResumeSection(
        id = 1L, versionId = 1L, sectionType = "EXPERIENCE",
        title = "Experience", content = "Android Engineer at Acme"
    )
    private val version = ResumeVersion(
        id = 1L, resumeId = 1L, versionName = "Original Import",
        sections = listOf(section)
    )
    private val resume = Resume(id = 1L, name = "resume.pdf", primaryVersionId = 1L)

    private val longJd = "Senior Android Engineer with 5+ years building scalable mobile " +
        "applications using Kotlin, Jetpack Compose, and Clean Architecture."

    private fun createViewModel() = ResumeEngineViewModel(
        mockRepository, mockImport, mockParse, mockCalculateAts,
        mockImprove, mockExport, mockTrackEvent, mockPdfExporter, mockDocxExporter
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { mockTrackEvent(any()) } returns Result.Success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Import`() = runTest {
        val viewModel = createViewModel()
        assertEquals(ResumeEngineState.Import, viewModel.state.value)
    }

    @Test
    fun `import success transitions to Preview with parsed sections`() = runTest {
        coEvery { mockImport.invoke(any()) } returns Result.Success(1L)
        coEvery { mockRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(version)))
        coEvery { mockRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeEngineEvent.ImportFile(mockk<Uri>()))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ResumeEngineState.Preview)
        assertEquals(1, (state as ResumeEngineState.Preview).version.sections.size)
    }

    @Test
    fun `import failure enters Error and retry re-imports`() = runTest {
        coEvery { mockImport.invoke(any()) } returns Result.Failure(DomainError("Bad file"))

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeEngineEvent.ImportFile(mockk<Uri>()))
        testDispatcher.scheduler.advanceUntilIdle()

        val error = viewModel.state.value
        assertTrue(error is ResumeEngineState.Error)
        assertEquals("Import", (error as ResumeEngineState.Error).step)

        // Retry after an Import error re-runs the import.
        coEvery { mockImport.invoke(any()) } returns Result.Success(1L)
        coEvery { mockRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(version)))
        coEvery { mockRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))

        viewModel.onEvent(ResumeEngineEvent.Retry)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value is ResumeEngineState.Preview)
    }

    @Test
    fun `continue from preview transitions to AtsScanning`() = runTest {
        coEvery { mockImport.invoke(any()) } returns Result.Success(1L)
        coEvery { mockRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(version)))
        coEvery { mockRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeEngineEvent.ImportFile(mockk<Uri>()))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ResumeEngineEvent.ContinueFromPreview)

        assertTrue(viewModel.state.value is ResumeEngineState.AtsScanning)
    }

    @Test
    fun `ats scan with short JD stays on AtsScanning`() = runTest {
        coEvery { mockImport.invoke(any()) } returns Result.Success(1L)
        coEvery { mockRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(version)))
        coEvery { mockRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeEngineEvent.ImportFile(mockk<Uri>()))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ResumeEngineEvent.ContinueFromPreview)
        viewModel.onEvent(ResumeEngineEvent.UpdateJdText("too short"))
        viewModel.onEvent(ResumeEngineEvent.RunAtsScan)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is ResumeEngineState.AtsScanning)
    }

    @Test
    fun `ats scan success transitions to AtsResult`() = runTest {
        coEvery { mockImport.invoke(any()) } returns Result.Success(1L)
        coEvery { mockRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(version)))
        coEvery { mockRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))
        coEvery { mockCalculateAts.invoke(any()) } returns Result.Success(
            AtsScoreResponse(
                atsResult = AtsResult(score = 80, resumeName = "resume.pdf", feedback = "Good"),
                analysis = ResumeAnalysis(
                    overallScore = 80,
                    matchingKeywords = listOf("Kotlin"),
                    missingKeywords = listOf("Rust")
                )
            )
        )

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeEngineEvent.ImportFile(mockk<Uri>()))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ResumeEngineEvent.ContinueFromPreview)
        viewModel.onEvent(ResumeEngineEvent.UpdateJdText(longJd))
        viewModel.onEvent(ResumeEngineEvent.RunAtsScan)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ResumeEngineState.AtsResult)
        assertEquals(80, (state as ResumeEngineState.AtsResult).score)
    }

    @Test
    fun `ats failure enters Error and back restores AtsScanning`() = runTest {
        coEvery { mockImport.invoke(any()) } returns Result.Success(1L)
        coEvery { mockRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(version)))
        coEvery { mockRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))
        coEvery { mockCalculateAts.invoke(any()) } returns Result.Failure(DomainError("AI down"))

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeEngineEvent.ImportFile(mockk<Uri>()))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ResumeEngineEvent.ContinueFromPreview)
        viewModel.onEvent(ResumeEngineEvent.UpdateJdText(longJd))
        viewModel.onEvent(ResumeEngineEvent.RunAtsScan)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is ResumeEngineState.Error)

        // Back from an ATS error restores AtsScanning (not Import) via lastStableState.
        viewModel.onEvent(ResumeEngineEvent.Back)
        assertTrue(viewModel.state.value is ResumeEngineState.AtsScanning)
    }

    @Test
    fun `skip ats advances to Optimizing`() = runTest {
        coEvery { mockImport.invoke(any()) } returns Result.Success(1L)
        coEvery { mockRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(version)))
        coEvery { mockRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeEngineEvent.ImportFile(mockk<Uri>()))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ResumeEngineEvent.ContinueFromPreview)
        viewModel.onEvent(ResumeEngineEvent.SkipAts)

        assertTrue(viewModel.state.value is ResumeEngineState.Optimizing)
    }

    @Test
    fun `improve section adds suggestion and accept applies it`() = runTest {
        coEvery { mockImport.invoke(any()) } returns Result.Success(1L)
        coEvery { mockRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(version)))
        coEvery { mockRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))
        coEvery { mockImprove.invoke(any()) } returns Result.Success(
            version.copy(sections = listOf(section.copy(content = "Improved content")))
        )

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeEngineEvent.ImportFile(mockk<Uri>()))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ResumeEngineEvent.ContinueFromPreview)
        viewModel.onEvent(ResumeEngineEvent.SkipAts)

        viewModel.onEvent(ResumeEngineEvent.ImproveSection("Experience"))
        testDispatcher.scheduler.advanceUntilIdle()

        val optimizing = viewModel.state.value as ResumeEngineState.Optimizing
        assertEquals("Improved content", optimizing.suggestions["Experience"])

        viewModel.onEvent(ResumeEngineEvent.AcceptSuggestion("Experience"))
        val updated = viewModel.state.value as ResumeEngineState.Optimizing
        assertEquals("Improved content", updated.version.sections.first().content)
        assertTrue(updated.suggestions.isEmpty())
    }

    @Test
    fun `save version persists and transitions to Exporting`() = runTest {
        coEvery { mockImport.invoke(any()) } returns Result.Success(1L)
        coEvery { mockRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(version)))
        coEvery { mockRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))
        coEvery { mockRepository.saveVersion(any()) } returns Result.Success(42L)

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeEngineEvent.ImportFile(mockk<Uri>()))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ResumeEngineEvent.ContinueFromPreview)
        viewModel.onEvent(ResumeEngineEvent.SkipAts)
        viewModel.onEvent(ResumeEngineEvent.SaveVersion("v2 — Optimized"))
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is ResumeEngineState.Exporting)
        assertEquals(42L, (state as ResumeEngineState.Exporting).version.id)
    }

    @Test
    fun `save failure enters Error and back restores Optimizing`() = runTest {
        coEvery { mockImport.invoke(any()) } returns Result.Success(1L)
        coEvery { mockRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(version)))
        coEvery { mockRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))
        coEvery { mockRepository.saveVersion(any()) } returns Result.Failure(DomainError("Disk full"))

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeEngineEvent.ImportFile(mockk<Uri>()))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ResumeEngineEvent.ContinueFromPreview)
        viewModel.onEvent(ResumeEngineEvent.SkipAts)
        viewModel.onEvent(ResumeEngineEvent.SaveVersion("v2 — Optimized"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is ResumeEngineState.Error)

        // Back from a Save error restores Optimizing (not a stuck Saving spinner).
        viewModel.onEvent(ResumeEngineEvent.Back)
        assertTrue(viewModel.state.value is ResumeEngineState.Optimizing)
    }

    @Test
    fun `finish resets state to Import`() = runTest {
        coEvery { mockImport.invoke(any()) } returns Result.Success(1L)
        coEvery { mockRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(version)))
        coEvery { mockRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))
        coEvery { mockRepository.saveVersion(any()) } returns Result.Success(42L)

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeEngineEvent.ImportFile(mockk<Uri>()))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ResumeEngineEvent.ContinueFromPreview)
        viewModel.onEvent(ResumeEngineEvent.SkipAts)
        viewModel.onEvent(ResumeEngineEvent.SaveVersion("v2"))
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(ResumeEngineEvent.Finish)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(ResumeEngineState.Import, viewModel.state.value)
    }

    @Test
    fun `back from Preview returns to Import`() = runTest {
        coEvery { mockImport.invoke(any()) } returns Result.Success(1L)
        coEvery { mockRepository.getVersions(1L) } returns flowOf(Result.Success(listOf(version)))
        coEvery { mockRepository.getResumeById(1L) } returns flowOf(Result.Success(resume))

        val viewModel = createViewModel()
        viewModel.onEvent(ResumeEngineEvent.ImportFile(mockk<Uri>()))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(ResumeEngineEvent.Back)

        assertEquals(ResumeEngineState.Import, viewModel.state.value)
    }
}
