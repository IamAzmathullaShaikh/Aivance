package com.bangersoul.aivance.feature.coverletter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.ExportCoverLetterRequest
import com.bangersoul.aivance.core.domain.usecase.coverletter.ExportCoverLetterUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.ExportLetterFormat
import com.bangersoul.aivance.core.domain.usecase.coverletter.GenerateCoverLetterRequest
import com.bangersoul.aivance.core.domain.usecase.coverletter.GenerateCoverLetterUseCase
import com.bangersoul.aivance.core.domain.usecase.coverletter.ImproveCoverLetterRequest
import com.bangersoul.aivance.core.domain.usecase.coverletter.ImproveCoverLetterUseCase
import com.bangersoul.aivance.feature.coverletter.domain.model.LetterTone
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CoverLetterUiState {
    data object Idle : CoverLetterUiState
    data object Generating : CoverLetterUiState
    data class Success(val content: String) : CoverLetterUiState
    data class Error(val message: String) : CoverLetterUiState
}

sealed interface CoverLetterUiEvent {
    data class Generate(
        val companyName: String,
        val role: String,
        val jobDescription: String,
        val tone: LetterTone = LetterTone.PROFESSIONAL
    ) : CoverLetterUiEvent
    data class Improve(val coverLetterId: Long, val feedback: String = "") : CoverLetterUiEvent
    data class Export(val coverLetterId: Long, val format: ExportLetterFormat = ExportLetterFormat.TXT) : CoverLetterUiEvent
    data object CopyToClipboard : CoverLetterUiEvent
    data object Reset : CoverLetterUiEvent
}

sealed interface CoverLetterUiEffect {
    data class ShowSnackbar(val message: String) : CoverLetterUiEffect
    data class CopyToClipboard(val text: String) : CoverLetterUiEffect
    data class ExportResult(val path: String) : CoverLetterUiEffect
}

@HiltViewModel
class CoverLetterViewModel @Inject constructor(
    private val generateCoverLetterUseCase: GenerateCoverLetterUseCase,
    private val improveCoverLetterUseCase: ImproveCoverLetterUseCase,
    private val exportCoverLetterUseCase: ExportCoverLetterUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CoverLetterUiState>(CoverLetterUiState.Idle)
    val uiState: StateFlow<CoverLetterUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CoverLetterUiEffect>(Channel.BUFFERED)
    val effects: Flow<CoverLetterUiEffect> = _effects.receiveAsFlow()

    private val _resumeText = MutableStateFlow("")
    val resumeText: StateFlow<String> = _resumeText.asStateFlow()

    private val _jobDescription = MutableStateFlow("")
    val jobDescription: StateFlow<String> = _jobDescription.asStateFlow()

    private val _selectedTone = MutableStateFlow(LetterTone.PROFESSIONAL)
    val selectedTone: StateFlow<LetterTone> = _selectedTone.asStateFlow()

    private var currentCoverLetterId = 0L
    private var currentContent = ""

    fun onEvent(event: CoverLetterUiEvent) {
        when (event) {
            is CoverLetterUiEvent.Generate -> generate(event.companyName, event.role, event.jobDescription, event.tone)
            is CoverLetterUiEvent.Improve -> improve(event.coverLetterId, event.feedback)
            is CoverLetterUiEvent.Export -> export(event.coverLetterId, event.format)
            CoverLetterUiEvent.CopyToClipboard -> copyToClipboard()
            CoverLetterUiEvent.Reset -> reset()
        }
    }

    private fun generate(companyName: String, role: String, jobDescription: String, tone: LetterTone) {
        if (companyName.isBlank() || role.isBlank()) {
            _uiState.value = CoverLetterUiState.Error("Company name and role are required")
            return
        }
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "cover_letter_generate"))

            val request = GenerateCoverLetterRequest(
                companyName = companyName,
                role = role,
                jobDescription = jobDescription,
                tone = when (tone) {
                    LetterTone.PROFESSIONAL -> com.bangersoul.aivance.core.common.enums.LetterTone.PROFESSIONAL
                    LetterTone.ENTHUSIASTIC -> com.bangersoul.aivance.core.common.enums.LetterTone.ENTHUSIASTIC
                    LetterTone.CONFIDENT -> com.bangersoul.aivance.core.common.enums.LetterTone.CONFIDENT
                    LetterTone.CREATIVE -> com.bangersoul.aivance.core.common.enums.LetterTone.CREATIVE
                }
            )
            val result = generateCoverLetterUseCase(request)
            when (result) {
                is CoreResult.Success -> {
                    currentCoverLetterId = result.data.id
                    currentContent = result.data.content
                    _uiState.value = CoverLetterUiState.Success(result.data.content)
                }
                is CoreResult.Failure -> {
                    _uiState.value = CoverLetterUiState.Error(result.error.message ?: "Generation failed")
                }
            }
        }
    }

    private fun improve(coverLetterId: Long, feedback: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "cover_letter_improve"))
            val request = ImproveCoverLetterRequest(coverLetterId = coverLetterId, feedback = feedback)
            val result = improveCoverLetterUseCase(request)
            when (result) {
                is CoreResult.Success -> {
                    currentContent = result.data.content
                    _uiState.value = CoverLetterUiState.Success(result.data.content)
                }
                is CoreResult.Failure -> {
                    _uiState.value = CoverLetterUiState.Error(result.error.message ?: "Improvement failed")
                }
            }
        }
    }

    private fun export(coverLetterId: Long, format: ExportLetterFormat) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "cover_letter_export"))
            val request = ExportCoverLetterRequest(coverLetterId = coverLetterId, format = format)
            val result = exportCoverLetterUseCase(request)
            when (result) {
                is CoreResult.Success -> {
                    sendEffect(CoverLetterUiEffect.ExportResult(result.data))
                    sendEffect(CoverLetterUiEffect.ShowSnackbar("Exported"))
                }
                is CoreResult.Failure -> {
                    sendEffect(CoverLetterUiEffect.ShowSnackbar(result.error.message ?: "Export failed"))
                }
            }
        }
    }

    private fun copyToClipboard() {
        if (currentContent.isNotBlank()) {
            sendEffect(CoverLetterUiEffect.CopyToClipboard(currentContent))
            sendEffect(CoverLetterUiEffect.ShowSnackbar("Copied to clipboard"))
        }
    }

    fun updateResumeText(text: String) { _resumeText.value = text }
    fun updateJobDescription(text: String) { _jobDescription.value = text }
    fun updateTone(tone: LetterTone) { _selectedTone.value = tone }

    private fun reset() {
        _uiState.value = CoverLetterUiState.Idle
        currentContent = ""
        currentCoverLetterId = 0L
    }

    private fun sendEffect(effect: CoverLetterUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
