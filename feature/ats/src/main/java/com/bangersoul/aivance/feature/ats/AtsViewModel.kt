package com.bangersoul.aivance.feature.ats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.AtsReport
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AtsRepository
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.ats.AnalyzeJobDescriptionUseCase
import com.bangersoul.aivance.core.domain.usecase.ats.AtsAnalysisRequest
import com.bangersoul.aivance.core.domain.usecase.ats.PerformAtsAnalysisUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AtsUiState {
    data object SelectingResume : AtsUiState
    data class InputJobDescription(val resume: Resume, val selectedVersion: ResumeVersion) : AtsUiState
    data object Analyzing : AtsUiState
    data class DisplayReport(val report: AtsReport) : AtsUiState
    data class Error(val message: String) : AtsUiState
}

@HiltViewModel
class AtsViewModel @Inject constructor(
    private val resumeRepository: ResumeRepository,
    private val analyzeJobDescriptionUseCase: AnalyzeJobDescriptionUseCase,
    private val performAtsAnalysisUseCase: PerformAtsAnalysisUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AtsUiState>(AtsUiState.SelectingResume)
    val uiState: StateFlow<AtsUiState> = _uiState.asStateFlow()

    private val _resumes = MutableStateFlow<List<Resume>>(emptyList())
    val resumes: StateFlow<List<Resume>> = _resumes.asStateFlow()

    private val _effects = Channel<AtsUiEffect>(Channel.BUFFERED)
    val effects: Flow<AtsUiEffect> = _effects.receiveAsFlow()

    private var currentResume: Resume? = null
    private var currentVersion: ResumeVersion? = null

    init {
        loadResumes()
    }

    fun onEvent(event: AtsUiEvent) {
        when (event) {
            AtsUiEvent.Start -> _uiState.value = AtsUiState.SelectingResume
            is AtsUiEvent.SelectResumeVersion -> {
                currentResume = event.resume
                currentVersion = event.version
                _uiState.value = AtsUiState.InputJobDescription(event.resume, event.version)
            }
            is AtsUiEvent.Analyze -> analyze(event.jobDescriptionText)
            AtsUiEvent.Reset -> {
                currentResume = null
                currentVersion = null
                _uiState.value = AtsUiState.SelectingResume
            }
        }
    }

    private fun loadResumes() {
        viewModelScope.launch {
            resumeRepository.getResumes().collect { result ->
                when (result) {
                    is Result.Success -> _resumes.value = result.data
                    is Result.Failure -> _uiState.value = AtsUiState.Error("Failed to load resumes: ${result.error.message}")
                }
            }
        }
    }

    private fun analyze(jdText: String) {
        val resumeId = currentResume?.id ?: return
        val versionId = currentVersion?.id ?: return

        viewModelScope.launch {
            _uiState.value = AtsUiState.Analyzing
            trackEventUseCase(TrackEventRequest("ats_analyze_start"))

            // Step 1: Parse JD
            val jdResult = analyzeJobDescriptionUseCase(jdText)
            val jdId = when (jdResult) {
                is Result.Success -> jdResult.data
                is Result.Failure -> {
                    _uiState.value = AtsUiState.Error("Failed to parse Job Description: ${jdResult.error.message}")
                    return@launch
                }
            }

            // Step 2: Perform Match Analysis
            val analysisResult = performAtsAnalysisUseCase(
                AtsAnalysisRequest(resumeId, versionId, jdId)
            )

            when (analysisResult) {
                is Result.Success -> {
                    _uiState.value = AtsUiState.DisplayReport(analysisResult.data)
                    trackEventUseCase(TrackEventRequest("ats_analyze_success"))
                }
                is Result.Failure -> {
                    _uiState.value = AtsUiState.Error("Analysis failed: ${analysisResult.error.message}")
                }
            }
        }
    }
}
