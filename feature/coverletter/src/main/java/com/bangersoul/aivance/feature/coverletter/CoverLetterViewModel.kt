package com.bangersoul.aivance.feature.coverletter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.model.CoverLetterVersion
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
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
        val isGenerating: Boolean = false
    ) : CoverLetterUiState
    data class Error(val message: String) : CoverLetterUiState
}

sealed interface CoverLetterUiEvent {
    data class Generate(val resumeId: Long, val versionId: Long, val jobId: Long, val recruiterId: String?) : CoverLetterUiEvent
    data class RegenerateSection(val versionId: Long, val sectionType: String) : CoverLetterUiEvent
    data object Load : CoverLetterUiEvent
}

@HiltViewModel
class CoverLetterViewModel @Inject constructor(
    private val coverLetterRepository: CoverLetterRepository,
    private val generateCoverLetterUseCase: GenerateCoverLetterUseCase,
    private val regenerateCoverLetterSectionUseCase: RegenerateCoverLetterSectionUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CoverLetterUiState>(CoverLetterUiState.Idle)
    val uiState: StateFlow<CoverLetterUiState> = _uiState.asStateFlow()

    private val _effects = Channel<Unit>(Channel.BUFFERED)
    val effects: Flow<Unit> = _effects.receiveAsFlow()

    fun onEvent(event: CoverLetterUiEvent) {
        when (event) {
            is CoverLetterUiEvent.Generate -> generate(event)
            is CoverLetterUiEvent.RegenerateSection -> regenerateSection(event)
            CoverLetterUiEvent.Load -> load()
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
                        _uiState.value = CoverLetterUiState.Error("Failed to load cover letters: ${result.error.message}")
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
                _uiState.value = CoverLetterUiState.Error("Generation failed")
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
            }
        }
    }
}
