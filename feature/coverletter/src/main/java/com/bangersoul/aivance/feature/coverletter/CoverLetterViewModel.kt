package com.bangersoul.aivance.feature.coverletter

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.model.CoverLetterVersion
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import com.bangersoul.aivance.core.util.PdfExporter
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.GenerateCoverLetterRequest
import com.bangersoul.aivance.core.domain.usecase.coverletter.GenerateCoverLetterUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.RegenerateCoverLetterSectionUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.RegenerateSectionRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CoverLetterUiState {
    data object Idle : CoverLetterUiState
    data object Loading : CoverLetterUiState
    data class Success(
        val coverLetter: CoverLetter? = null,
        val selectedVersion: CoverLetterVersion? = null,
        val isGenerating: Boolean = false,
        val isEditing: Boolean = false,
        /** Section index → draft text, populated when the user enters edit mode.
         *  Keyed by index (not section id) because freshly generated sections can
         *  share the default id 0, which would collapse drafts into one entry. */
        val sectionDrafts: Map<Int, String> = emptyMap()
    ) : CoverLetterUiState
    data class Error(val message: String) : CoverLetterUiState
}

sealed interface CoverLetterUiEvent {
    data class Generate(
        val resumeId: Long,
        val versionId: Long,
        val jobId: Long,
        val recruiterId: String?
    ) : CoverLetterUiEvent

    data class RegenerateSection(val versionId: Long, val sectionType: String) : CoverLetterUiEvent
    data object Load : CoverLetterUiEvent

    /** Toggles between read-only and inline-edit mode. */
    data object ToggleEdit : CoverLetterUiEvent
    data class UpdateSection(val sectionIndex: Int, val content: String) : CoverLetterUiEvent
    data object SaveEdits : CoverLetterUiEvent
    data object CopyAll : CoverLetterUiEvent
    data object Export : CoverLetterUiEvent
}

sealed interface CoverLetterUiEffect {
    data class ShowSnackbar(val message: String) : CoverLetterUiEffect
    data class CopyText(val text: String) : CoverLetterUiEffect
    data class ExportPdf(val uri: Uri) : CoverLetterUiEffect
}

@HiltViewModel
class CoverLetterViewModel @Inject constructor(
    private val coverLetterRepository: CoverLetterRepository,
    private val generateCoverLetterUseCase: GenerateCoverLetterUseCase,
    private val regenerateCoverLetterSectionUseCase: RegenerateCoverLetterSectionUseCase,
    private val trackEventUseCase: TrackEventUseCase,
    private val pdfExporter: PdfExporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<CoverLetterUiState>(CoverLetterUiState.Idle)
    val uiState: StateFlow<CoverLetterUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CoverLetterUiEffect>(Channel.BUFFERED)
    val effects: Flow<CoverLetterUiEffect> = _effects.receiveAsFlow()

    fun onEvent(event: CoverLetterUiEvent) {
        when (event) {
            is CoverLetterUiEvent.Generate -> generate(event)
            is CoverLetterUiEvent.RegenerateSection -> regenerateSection(event)
            CoverLetterUiEvent.Load -> load()
            CoverLetterUiEvent.ToggleEdit -> toggleEdit()
            is CoverLetterUiEvent.UpdateSection -> updateSection(event.sectionIndex, event.content)
            CoverLetterUiEvent.SaveEdits -> saveEdits()
            CoverLetterUiEvent.CopyAll -> copyAll()
            CoverLetterUiEvent.Export -> export()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = CoverLetterUiState.Loading
            coverLetterRepository.getCoverLetters().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val list = result.data
                        if (list.isNotEmpty()) {
                            val first = list.first()
                            _uiState.value = CoverLetterUiState.Success(coverLetter = first)
                        } else {
                            _uiState.value = CoverLetterUiState.Idle
                        }
                    }
                    is Result.Failure -> {
                        _uiState.value = CoverLetterUiState.Error(
                            "Failed to load cover letters: ${result.error.message}"
                        )
                    }
                }
            }
        }
    }

    private fun generate(event: CoverLetterUiEvent.Generate) {
        viewModelScope.launch {
            _uiState.value = CoverLetterUiState.Loading
            trackEventUseCase(TrackEventRequest("cover_letter_generate_start"))

            val result = generateCoverLetterUseCase(
                GenerateCoverLetterRequest(
                    resumeId = event.resumeId,
                    resumeVersionId = event.versionId,
                    jobId = event.jobId,
                    recruiterId = event.recruiterId
                )
            )

            if (result is Result.Success) {
                load()
            } else {
                _uiState.value = CoverLetterUiState.Error(
                    (result as? Result.Failure)?.error?.message ?: "Generation failed"
                )
            }
        }
    }

    private fun regenerateSection(event: CoverLetterUiEvent.RegenerateSection) {
        viewModelScope.launch {
            val current = _uiState.value as? CoverLetterUiState.Success ?: return@launch
            _uiState.value = current.copy(isGenerating = true)

            val result = regenerateCoverLetterSectionUseCase(
                RegenerateSectionRequest(event.versionId, event.sectionType)
            )

            if (result is Result.Success) {
                load()
            } else {
                _uiState.value = current.copy(isGenerating = false)
                _effects.send(
                    CoverLetterUiEffect.ShowSnackbar(
                        (result as? Result.Failure)?.error?.message ?: "Failed to regenerate section"
                    )
                )
            }
        }
    }

    private fun toggleEdit() {
        val current = _uiState.value as? CoverLetterUiState.Success ?: return
        val version = current.selectedVersion ?: current.coverLetter?.versions?.firstOrNull() ?: return
        if (current.isEditing) {
            // Leaving edit mode discards unsaved drafts.
            _uiState.value = current.copy(isEditing = false, sectionDrafts = emptyMap())
        } else {
            _uiState.value = current.copy(
                isEditing = true,
                sectionDrafts = version.sections.mapIndexed { index, section -> index to section.content }.toMap()
            )
        }
    }

    private fun updateSection(sectionIndex: Int, content: String) {
        val current = _uiState.value as? CoverLetterUiState.Success ?: return
        if (!current.isEditing) return
        _uiState.value = current.copy(sectionDrafts = current.sectionDrafts + (sectionIndex to content))
    }

    private fun saveEdits() {
        val current = _uiState.value as? CoverLetterUiState.Success ?: return
        val version = current.selectedVersion ?: current.coverLetter?.versions?.firstOrNull() ?: return
        if (!current.isEditing) return

        viewModelScope.launch {
            val updated = version.copy(
                lastModified = System.currentTimeMillis(),
                sections = version.sections.mapIndexed { index, section ->
                    current.sectionDrafts[index]?.let { section.copy(content = it) } ?: section
                }
            )
            val result = coverLetterRepository.saveVersion(updated)
            if (result is Result.Success) {
                trackEventUseCase(TrackEventRequest("cover_letter_edit_save"))
                _uiState.value = current.copy(isEditing = false, sectionDrafts = emptyMap())
                load()
                _effects.send(CoverLetterUiEffect.ShowSnackbar("Cover letter updated"))
            } else {
                val failure = result as? Result.Failure
                _effects.send(
                    CoverLetterUiEffect.ShowSnackbar(
                        failure?.error?.message?.takeIf { it.isNotBlank() } ?: "Failed to save edits"
                    )
                )
            }
        }
    }

    private fun copyAll() {
        val current = _uiState.value as? CoverLetterUiState.Success ?: return
        val version = current.selectedVersion ?: current.coverLetter?.versions?.firstOrNull() ?: return
        val fullText = version.sections.joinToString("\n\n") { it.content }
        if (fullText.isBlank()) return

        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("cover_letter_copy"))
            _effects.send(CoverLetterUiEffect.CopyText(fullText))
            _effects.send(CoverLetterUiEffect.ShowSnackbar("Cover letter copied"))
        }
    }

    private fun export() {
        val current = _uiState.value as? CoverLetterUiState.Success ?: return
        val coverLetter = current.coverLetter ?: return
        val version = current.selectedVersion ?: coverLetter.versions.firstOrNull() ?: return

        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("cover_letter_export"))
            // Real PDF render (Phase 4 STEP 2) — resolves the old plaintext
            // share-sheet export. Section headings become PDF headers.
            val sections = version.sections.map { it.title to it.content }
            val result = pdfExporter.exportToPdf(
                title = "Cover Letter — ${coverLetter.company}",
                sections = sections
            )
            when (result) {
                is Result.Success -> {
                    _effects.send(CoverLetterUiEffect.ExportPdf(result.data))
                    _effects.send(CoverLetterUiEffect.ShowSnackbar("Cover letter exported as PDF"))
                }
                is Result.Failure -> {
                    _effects.send(
                        CoverLetterUiEffect.ShowSnackbar(
                            result.error.message.ifBlank { "Failed to export cover letter" }
                        )
                    )
                }
            }
        }
    }
}
