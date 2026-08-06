package com.bangersoul.aivance.feature.resume

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeAnalysis
import com.bangersoul.aivance.core.common.model.ResumeSection
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.util.DocxExporter
import com.bangersoul.aivance.core.util.PdfExporter
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.AtsScoreRequest
import com.bangersoul.aivance.core.domain.usecase.resume.CalculateATSScoreUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ExportFormat
import com.bangersoul.aivance.core.domain.usecase.resume.ExportResumeRequest
import com.bangersoul.aivance.core.domain.usecase.resume.ExportResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ImportResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ImproveResumeRequest
import com.bangersoul.aivance.core.domain.usecase.resume.ImproveResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ParseResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.StreamImproveSectionRequest
import com.bangersoul.aivance.core.domain.usecase.resume.StreamImproveSectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 7-step Resume Engine state machine: Import → Parsing → Preview → ATS Scan →
 * AI Optimization → Version Save → Export.
 *
 * Every transition is driven by a real use case; nothing is hardcoded.
 */
sealed interface ResumeEngineState {
    data object Import : ResumeEngineState
    data class Parsing(val progress: Float) : ResumeEngineState
    data class Preview(val resume: Resume, val version: ResumeVersion) : ResumeEngineState
    data class AtsScanning(val resume: Resume, val version: ResumeVersion, val jdText: String = "") : ResumeEngineState
    data class AtsResult(
        val resume: Resume,
        val version: ResumeVersion,
        val analysis: ResumeAnalysis,
        val score: Int,
        val jdText: String = ""
    ) : ResumeEngineState
    data class Optimizing(
        val resume: Resume,
        val version: ResumeVersion,
        val jdText: String = "",
        val sectionInProgress: String? = null,
        /** Live token stream for the section currently being improved — rendered
         *  with a typewriter effect inside the section card. Cleared once the
         *  full suggestion lands in [suggestions]. */
        val streamingContent: String? = null,
        val suggestions: Map<String, String> = emptyMap()
    ) : ResumeEngineState
    data class Saving(val resume: Resume, val version: ResumeVersion) : ResumeEngineState
    data class Exporting(val resume: Resume, val version: ResumeVersion) : ResumeEngineState
    data class Error(val step: String, val message: String) : ResumeEngineState {
        /**
         * Only Import/Parsing failures can be retried by re-importing; for
         * ATS/Optimization/Save errors retry just restores the prior step.
         */
        val canRetry: Boolean get() = step == "Import" || step == "Parsing"
    }
}

sealed interface ResumeEngineEvent {
    data class ImportFile(val uri: Uri) : ResumeEngineEvent
    /** Fires after ML Kit OCR extracts text from a camera capture. */
    data class ImportOcrText(val rawText: String) : ResumeEngineEvent
    data class UpdateSectionContent(val sectionTitle: String, val content: String) : ResumeEngineEvent
    data object ContinueFromPreview : ResumeEngineEvent
    data class UpdateJdText(val text: String) : ResumeEngineEvent
    data object RunAtsScan : ResumeEngineEvent
    data object SkipAts : ResumeEngineEvent
    data class ImproveSection(val sectionTitle: String) : ResumeEngineEvent
    data class AcceptSuggestion(val sectionTitle: String) : ResumeEngineEvent
    data class DiscardSuggestion(val sectionTitle: String) : ResumeEngineEvent
    data class SaveVersion(val versionName: String) : ResumeEngineEvent
    data object ExportPdf : ResumeEngineEvent
    data object ExportDocx : ResumeEngineEvent
    data object Finish : ResumeEngineEvent
    data object Retry : ResumeEngineEvent
    data object Back : ResumeEngineEvent
}

sealed interface ResumeEngineEffect {
    data class ShowSnackbar(val message: String) : ResumeEngineEffect
    data class ExportResult(val text: String, val fileName: String) : ResumeEngineEffect
    data class ExportPdf(val uri: Uri) : ResumeEngineEffect
    data class ExportDocx(val uri: Uri) : ResumeEngineEffect
    data object Finished : ResumeEngineEffect
}

@HiltViewModel
class ResumeEngineViewModel @Inject constructor(
    private val resumeRepository: ResumeRepository,
    private val importResumeUseCase: ImportResumeUseCase,
    private val parseResumeUseCase: ParseResumeUseCase,
    private val calculateATSScoreUseCase: CalculateATSScoreUseCase,
    private val improveResumeUseCase: ImproveResumeUseCase,
    private val streamImproveSectionUseCase: StreamImproveSectionUseCase,
    private val exportResumeUseCase: ExportResumeUseCase,
    private val trackEventUseCase: TrackEventUseCase,
    private val pdfExporter: PdfExporter,
    private val docxExporter: DocxExporter
) : ViewModel() {

    private val _state = MutableStateFlow<ResumeEngineState>(ResumeEngineState.Import)
    val state: StateFlow<ResumeEngineState> = _state.asStateFlow()

    private val _effects = Channel<ResumeEngineEffect>(Channel.BUFFERED)
    val effects: Flow<ResumeEngineEffect> = _effects.receiveAsFlow()

    /** Working copy of the version that accumulates accepted AI edits. */
    private var workingVersion: ResumeVersion? = null

    /** Last import URI so the Error step can offer a one-tap retry. */
    private var lastImportUri: Uri? = null

    /** State before the current Error, so back()/retry() can restore context
     *  (e.g. return to AtsScanning after an ATS failure instead of dropping the
     *  parsed resume back to Import). */
    private var lastStableState: ResumeEngineState = ResumeEngineState.Import

    fun onEvent(event: ResumeEngineEvent) {
        when (event) {
            is ResumeEngineEvent.ImportFile -> importFile(event.uri)
            is ResumeEngineEvent.ImportOcrText -> importOcrText(event.rawText)
            is ResumeEngineEvent.UpdateSectionContent -> updateSectionContent(event.sectionTitle, event.content)
            ResumeEngineEvent.ContinueFromPreview -> continueFromPreview()
            is ResumeEngineEvent.UpdateJdText -> updateJdText(event.text)
            ResumeEngineEvent.RunAtsScan -> runAtsScan()
            ResumeEngineEvent.SkipAts -> skipAts()
            is ResumeEngineEvent.ImproveSection -> improveSection(event.sectionTitle)
            is ResumeEngineEvent.AcceptSuggestion -> acceptSuggestion(event.sectionTitle)
            is ResumeEngineEvent.DiscardSuggestion -> discardSuggestion(event.sectionTitle)
            is ResumeEngineEvent.SaveVersion -> saveVersion(event.versionName)
            ResumeEngineEvent.ExportPdf -> exportPdf()
            ResumeEngineEvent.ExportDocx -> exportDocx()
            ResumeEngineEvent.Finish -> finish()
            ResumeEngineEvent.Retry -> retry()
            ResumeEngineEvent.Back -> back()
        }
    }

    private fun importOcrText(rawText: String) {
        if (rawText.isBlank()) {
            enterError("Import", "No text could be extracted from the photo. Try a clearer image or pick a PDF/DOCX.")
            return
        }
        viewModelScope.launch {
            _state.value = ResumeEngineState.Parsing(0.4f)
            trackEventUseCase(TrackEventRequest("resume_engine_ocr_import"))
            val resumeId = System.currentTimeMillis()
            val dateFormat = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
            val dateStr = dateFormat.format(java.util.Date())
            val version = ResumeVersion(
                id = 1L,
                resumeId = resumeId,
                versionName = "Camera Scan — $dateStr",
                sections = listOf(
                    ResumeSection(
                        id = 1L,
                        versionId = 1L,
                        sectionType = "general",
                        title = "Raw Text",
                        content = rawText,
                        sectionOrder = 0
                    )
                )
            )
            workingVersion = version
            val resume = Resume(id = resumeId, name = "Camera Scan Resume", rawText = rawText)
            _state.value = ResumeEngineState.Parsing(1f)
            _state.value = ResumeEngineState.Preview(resume, version)
            trackEventUseCase(TrackEventRequest("resume_engine_ocr_parsed"))
        }
    }

    private fun importFile(uri: Uri) {
        lastImportUri = uri
        viewModelScope.launch {
            _state.value = ResumeEngineState.Parsing(0.2f)
            trackEventUseCase(TrackEventRequest("resume_engine_import"))

            val importResult = importResumeUseCase(uri)
            val resumeId = when (importResult) {
                is Result.Success -> importResult.data
                is Result.Failure -> {
                    enterError("Import", importResult.error.message)
                    return@launch
                }
            }
            _state.value = ResumeEngineState.Parsing(0.6f)

            // The import already triggers an initial parse; ensure at least one
            // version exists before moving to the Preview step.
            var versions = (resumeRepository.getVersions(resumeId).firstOrNull() as? Result.Success)?.data
                ?: emptyList()
            if (versions.isEmpty()) {
                parseResumeUseCase(resumeId)
                versions = (resumeRepository.getVersions(resumeId).firstOrNull() as? Result.Success)?.data
                    ?: emptyList()
            }
            _state.value = ResumeEngineState.Parsing(1f)

            val version = versions.firstOrNull()
            val resume = (resumeRepository.getResumeById(resumeId).firstOrNull() as? Result.Success)?.data
                ?: Resume(id = resumeId, name = "Imported Resume")
            if (version == null) {
                // The parser now guarantees at least a heuristic section, but
                // stay resilient: if no version exists (e.g. extraction produced
                // nothing) fall back to a single Summary section of the raw text
                // rather than dead-ending the user on a parsing error.
                val rawText = resume.rawText
                if (rawText.isNullOrBlank()) {
                    enterError("Parsing", "No resume sections were found in the file.")
                    return@launch
                }
                val fallback = ResumeVersion(
                    resumeId = resumeId,
                    versionName = "Imported Content",
                    sections = listOf(
                        ResumeSection(
                            sectionType = "summary",
                            title = "Summary",
                            content = rawText.trim(),
                            sectionOrder = 0
                        )
                    )
                )
                workingVersion = fallback
                _state.value = ResumeEngineState.Preview(resume, fallback)
                trackEventUseCase(TrackEventRequest("resume_engine_parsed"))
                return@launch
            }
            workingVersion = version
            _state.value = ResumeEngineState.Preview(resume, version)
            trackEventUseCase(TrackEventRequest("resume_engine_parsed"))
        }
    }

    /** Inline section editing during the Preview step — persists into the working copy. */
    private fun updateSectionContent(sectionTitle: String, content: String) {
        val current = _state.value as? ResumeEngineState.Preview ?: return
        val version = workingVersion ?: current.version
        val updated = version.copy(
            sections = version.sections.map {
                if (it.title == sectionTitle) it.copy(content = content) else it
            }
        )
        workingVersion = updated
        _state.value = current.copy(version = updated)
    }

    private fun continueFromPreview() {
        val current = _state.value as? ResumeEngineState.Preview ?: return
        workingVersion = current.version
        _state.value = ResumeEngineState.AtsScanning(current.resume, current.version)
    }

    private fun updateJdText(text: String) {
        val current = _state.value as? ResumeEngineState.AtsScanning ?: return
        _state.value = current.copy(jdText = text)
    }

    private fun runAtsScan() {
        val current = _state.value as? ResumeEngineState.AtsScanning ?: return
        if (current.jdText.length <= 50) {
            viewModelScope.launch {
                _effects.send(ResumeEngineEffect.ShowSnackbar("Paste a job description (at least 50 characters) to run the scan."))
            }
            return
        }
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("resume_engine_ats"))
            val result = calculateATSScoreUseCase(
                AtsScoreRequest(
                    resumeId = current.resume.id,
                    versionId = current.version.id,
                    jobDescription = current.jdText
                )
            )
            when (result) {
                is Result.Success -> {
                    _state.value = ResumeEngineState.AtsResult(
                        resume = current.resume,
                        version = current.version,
                        analysis = result.data.analysis,
                        score = result.data.atsResult.score,
                        jdText = current.jdText
                    )
                }
                is Result.Failure -> enterError("ATS Scan", result.error.message)
            }
        }
    }

    private fun skipAts() {
        val current = _state.value
        val resume = (current as? ResumeEngineState.AtsScanning)?.resume
            ?: (current as? ResumeEngineState.AtsResult)?.resume
            ?: (current as? ResumeEngineState.Preview)?.resume
            ?: return
        val version = (current as? ResumeEngineState.AtsScanning)?.version
            ?: (current as? ResumeEngineState.AtsResult)?.version
            ?: (current as? ResumeEngineState.Preview)?.version
            ?: return
        val jdText = (current as? ResumeEngineState.AtsScanning)?.jdText
            ?: (current as? ResumeEngineState.AtsResult)?.jdText
            ?: ""
        _state.value = ResumeEngineState.Optimizing(resume, version, jdText = jdText)
    }

    private fun improveSection(sectionTitle: String) {
        val current = _state.value as? ResumeEngineState.Optimizing ?: return
        if (current.sectionInProgress != null) return
        _state.value = current.copy(sectionInProgress = sectionTitle, streamingContent = "")
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("resume_engine_improve"))
            var full = ""
            try {
                val currentVersion = workingVersion ?: current.version
                val sectionObj = currentVersion.sections.firstOrNull { it.title.equals(sectionTitle, ignoreCase = true) }
                streamImproveSectionUseCase.stream(
                    StreamImproveSectionRequest(
                        resumeId = current.resume.id,
                        versionId = current.version.id,
                        sectionTitle = sectionTitle,
                        jobDescription = current.jdText.ifBlank { null },
                        feedback = "Focus on making the '${sectionTitle}' section stronger and more impactful.",
                        sectionContent = sectionObj?.content
                    )
                ).collect { chunk ->
                    full += chunk
                    val latest = _state.value as? ResumeEngineState.Optimizing ?: return@collect
                    _state.value = latest.copy(
                        streamingContent = full
                    )
                }
                val latest = _state.value as? ResumeEngineState.Optimizing ?: return@launch
                _state.value = latest.copy(
                    sectionInProgress = null,
                    streamingContent = null,
                    suggestions = latest.suggestions + (sectionTitle to full)
                )
            } catch (e: Exception) {
                enterError("Optimization", e.message ?: "AI optimization failed")
            }
        }
    }

    private fun acceptSuggestion(sectionTitle: String) {
        val current = _state.value as? ResumeEngineState.Optimizing ?: return
        val suggestion = current.suggestions[sectionTitle] ?: return
        val version = workingVersion ?: current.version
        val updated = version.copy(
            sections = version.sections.map { section ->
                if (section.title == sectionTitle) section.copy(content = suggestion) else section
            }
        )
        workingVersion = updated
        _state.value = current.copy(
            version = updated,
            suggestions = current.suggestions - sectionTitle
        )
    }

    private fun discardSuggestion(sectionTitle: String) {
        val current = _state.value as? ResumeEngineState.Optimizing ?: return
        _state.value = current.copy(suggestions = current.suggestions - sectionTitle)
    }

    private fun saveVersion(versionName: String) {
        val current = _state.value as? ResumeEngineState.Optimizing ?: return
        val version = (workingVersion ?: current.version).copy(
            id = 0, // force a new version row
            versionName = versionName.ifBlank { "v${System.currentTimeMillis()}" },
            lastModified = System.currentTimeMillis()
        )
        _state.value = ResumeEngineState.Saving(current.resume, version)
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("resume_engine_save"))
            val result = resumeRepository.saveVersion(version)
            when (result) {
                is Result.Success -> {
                    val saved = version.copy(id = result.data)
                    workingVersion = saved
                    _state.value = ResumeEngineState.Exporting(current.resume, saved)
                    _effects.send(ResumeEngineEffect.ShowSnackbar("Version saved"))
                }
                is Result.Failure -> {
                    workingVersion = null
                    enterError("Save", result.error.message)
                }
            }
        }
    }

    /**
     * Real PDF render (Phase 4 STEP 2): the saved version's sections become
     * PDF headers + wrapped body text, shared via a FileProvider Uri.
     */
    private fun exportPdf() {
        val current = _state.value as? ResumeEngineState.Exporting ?: return
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("resume_engine_export_pdf"))
            val sections = current.version.sections.map { it.title to it.content }
            val result = pdfExporter.exportToPdf(
                title = current.version.versionName,
                sections = sections
            )
            when (result) {
                is Result.Success -> _effects.send(ResumeEngineEffect.ExportPdf(result.data))
                is Result.Failure -> _effects.send(
                    ResumeEngineEffect.ShowSnackbar("PDF export failed: ${result.error.message}")
                )
            }
        }
    }

    /**
     * Real DOCX render via Apache POI (DocxExporter): the saved version's sections
     * become formatted Word document paragraphs.
     */
    private fun exportDocx() {
        val current = _state.value as? ResumeEngineState.Exporting ?: return
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("resume_engine_export_docx"))
            val sections = current.version.sections.map { it.title to it.content }
            val result = docxExporter.exportToDocx(
                title = current.version.versionName,
                sections = sections
            )
            when (result) {
                is Result.Success -> _effects.send(ResumeEngineEffect.ExportDocx(result.data))
                is Result.Failure -> _effects.send(
                    ResumeEngineEffect.ShowSnackbar("DOCX export failed: ${result.error.message}")
                )
            }
        }
    }

    /**
     * Re-runs the last import from the Error step when the failure was
     * recoverable. Only Import/Parsing failures can be retried by re-importing;
     * for ATS/Optimization failures (which already have a parsed resume) retry
     * restores the pre-error step — re-importing would duplicate the resume row.
     */
    private fun retry() {
        val error = _state.value as? ResumeEngineState.Error ?: return
        if (error.canRetry) {
            val uri = lastImportUri
            if (uri != null) {
                importFile(uri)
            } else {
                _state.value = ResumeEngineState.Import
            }
        } else {
            back()
        }
    }

    private fun enterError(step: String, message: String) {
        // Normalize the pre-error state so back()/retry() never restore a
        // transient screen: Parsing → Import (a static progress view has no
        // way forward), and Optimizing → clear the in-progress marker (an
        // eternal "Improving with AI…" spinner with no suggestion would dead-end
        // the user since the Improve button is hidden while sectionInProgress is set).
        lastStableState = when (val s = _state.value) {
            is ResumeEngineState.Parsing -> ResumeEngineState.Import
            is ResumeEngineState.Optimizing -> s.copy(sectionInProgress = null)
            // Saving is transient with no interactive UI; restore the Optimization
            // step so the user can re-save after a failed save instead of landing
            // on an eternal progress spinner.
            is ResumeEngineState.Saving -> ResumeEngineState.Optimizing(s.resume, s.version)
            else -> s
        }
        _state.value = ResumeEngineState.Error(step, message)
    }

    private fun finish() {
        // Reset the state machine so the next visit to the Resume tab starts
        // fresh at Import instead of showing the stale final step. The Finished
        // effect tells the nav graph to navigate back (no-op for a root tab,
        // but the state reset is what matters here).
        workingVersion = null
        _state.value = ResumeEngineState.Import
        viewModelScope.launch {
            _effects.send(ResumeEngineEffect.Finished)
        }
    }

    private fun back() {
        _state.value = when (val current = _state.value) {
            is ResumeEngineState.Parsing -> ResumeEngineState.Import
            is ResumeEngineState.Preview -> ResumeEngineState.Import
            is ResumeEngineState.AtsScanning -> ResumeEngineState.Preview(current.resume, current.version)
            is ResumeEngineState.AtsResult -> ResumeEngineState.AtsScanning(current.resume, current.version, "")
            is ResumeEngineState.Optimizing -> ResumeEngineState.AtsScanning(current.resume, current.version, current.jdText)
            is ResumeEngineState.Saving -> ResumeEngineState.Optimizing(current.resume, current.version)
            // Saving is a transient state (auto-flips to Exporting on save), so
            // back from Exporting returns to Optimization rather than a stuck spinner.
            is ResumeEngineState.Exporting -> ResumeEngineState.Optimizing(current.resume, current.version)
            // Restore the pre-error step so back/retry after an ATS or
            // Optimization failure doesn't lose the parsed resume context.
            is ResumeEngineState.Error -> lastStableState
            is ResumeEngineState.Import -> ResumeEngineState.Import
        }
    }
}
