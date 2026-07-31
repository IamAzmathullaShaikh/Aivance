package com.bangersoul.aivance.feature.resume

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.AnalyseResumeRequest
import com.bangersoul.aivance.core.domain.usecase.resume.AnalyseResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.AtsScoreRequest
import com.bangersoul.aivance.core.domain.usecase.resume.CalculateATSScoreUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ExportFormat
import com.bangersoul.aivance.core.domain.usecase.resume.ExportResumeRequest
import com.bangersoul.aivance.core.domain.usecase.resume.ExportResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.GenerateResumeSummaryUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ImproveResumeRequest
import com.bangersoul.aivance.core.domain.usecase.resume.ImproveResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ImportResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ResumeSummaryRequest
import com.bangersoul.aivance.feature.resume.domain.model.KeywordInfo
import com.bangersoul.aivance.feature.resume.domain.model.OptimizationTip
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ResumeUiState {
    data object Idle : ResumeUiState
    data object Loading : ResumeUiState
    data class Success(
        val resume: Resume? = null,
        val versions: List<ResumeVersion> = emptyList(),
        val selectedVersion: ResumeVersion? = null,
        val atsScore: Int? = null,
        val analysisResult: com.bangersoul.aivance.feature.resume.domain.model.ResumeAnalysis? = null
    ) : ResumeUiState
    data class Error(val message: String) : ResumeUiState
}

sealed interface ResumeUiEvent {
    data class ImportFile(val uri: Uri) : ResumeUiEvent
    data class SelectVersion(val versionId: Long) : ResumeUiEvent
    data class Analyze(val jobDescription: String) : ResumeUiEvent
    data class SaveVersion(val version: ResumeVersion) : ResumeUiEvent
    data class Export(val format: ExportFormat) : ResumeUiEvent
    data object Refresh : ResumeUiEvent
}

sealed interface ResumeUiEffect {
    data class ShowSnackbar(val message: String) : ResumeUiEffect
    data class ExportResult(val path: String) : ResumeUiEffect
}

@HiltViewModel
class ResumeViewModel @Inject constructor(
    private val resumeRepository: ResumeRepository,
    private val analyseResumeUseCase: AnalyseResumeUseCase,
    private val calculateATSScoreUseCase: CalculateATSScoreUseCase,
    private val improveResumeUseCase: ImproveResumeUseCase,
    private val generateResumeSummaryUseCase: GenerateResumeSummaryUseCase,
    private val exportResumeUseCase: ExportResumeUseCase,
    private val importResumeUseCase: ImportResumeUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResumeUiState>(ResumeUiState.Idle)
    val uiState: StateFlow<ResumeUiState> = _uiState.asStateFlow()

    private val _effects = Channel<ResumeUiEffect>(Channel.BUFFERED)
    val effects: Flow<ResumeUiEffect> = _effects.receiveAsFlow()

    private var currentResumeId: Long = 0L
    private var currentVersionId: Long = 0L

    fun onEvent(event: ResumeUiEvent) {
        when (event) {
            is ResumeUiEvent.ImportFile -> importFile(event.uri)
            is ResumeUiEvent.SelectVersion -> selectVersion(event.versionId)
            is ResumeUiEvent.Analyze -> analyze(event.jobDescription)
            is ResumeUiEvent.SaveVersion -> saveVersion(event.version)
            is ResumeUiEvent.Export -> export(event.format)
            ResumeUiEvent.Refresh -> loadResumes()
        }
    }

    private fun loadResumes() {
        viewModelScope.launch {
            _uiState.value = ResumeUiState.Loading
            resumeRepository.getResumes().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val resumes = result.data
                        if (resumes.isNotEmpty()) {
                            val primary = resumes.firstOrNull { it.id == currentResumeId } ?: resumes.first()
                            currentResumeId = primary.id
                            loadVersions(primary.id)
                        } else {
                            _uiState.value = ResumeUiState.Idle
                        }
                    }
                    is Result.Failure -> _uiState.value = ResumeUiState.Error(result.error.message)
                }
            }
        }
    }

    private fun loadVersions(resumeId: Long) {
        viewModelScope.launch {
            resumeRepository.getVersions(resumeId).collect { result ->
                if (result is Result.Success) {
                    val versions = result.data
                    val selected = versions.find { it.id == currentVersionId } ?: versions.firstOrNull()
                    currentVersionId = selected?.id ?: 0L

                    val currentState = _uiState.value
                    if (currentState is ResumeUiState.Success) {
                        _uiState.value = currentState.copy(versions = versions, selectedVersion = selected)
                    } else {
                        _uiState.value = ResumeUiState.Success(versions = versions, selectedVersion = selected)
                    }
                }
            }
        }
    }

    private fun importFile(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = ResumeUiState.Loading
            trackEventUseCase(TrackEventRequest("resume_import"))
            val result = importResumeUseCase(uri)
            when (result) {
                is Result.Success -> {
                    currentResumeId = result.data
                    loadResumes()
                }
                is Result.Failure -> _uiState.value = ResumeUiState.Error(result.error.message)
            }
        }
    }

    private fun selectVersion(versionId: Long) {
        currentVersionId = versionId
        loadVersions(currentResumeId)
    }

    private fun analyze(jobDescription: String) {
        viewModelScope.launch {
            val currentState = _uiState.value as? ResumeUiState.Success ?: return@launch
            _uiState.value = currentState.copy(atsScore = null, analysisResult = null)

            val request = AtsScoreRequest(
                resumeId = currentResumeId,
                versionId = currentVersionId,
                jobDescription = jobDescription
            )

            val result = calculateATSScoreUseCase(request)
            if (result is Result.Success) {
                val data = result.data
                _uiState.value = currentState.copy(
                    atsScore = data.atsResult.score,
                    analysisResult = com.bangersoul.aivance.feature.resume.domain.model.ResumeAnalysis(
                        matchScore = data.analysis.overallScore,
                        keywords = data.analysis.matchingKeywords.map { KeywordInfo(it, true) } +
                                  data.analysis.missingKeywords.map { KeywordInfo(it, false) },
                        tips = data.analysis.suggestions.map { OptimizationTip("Improvement", it) }
                    )
                )
            }
        }
    }

    private fun saveVersion(version: ResumeVersion) {
        viewModelScope.launch {
            resumeRepository.saveVersion(version)
            loadVersions(currentResumeId)
            sendEffect(ResumeUiEffect.ShowSnackbar("Version saved"))
        }
    }

    private fun export(format: ExportFormat) {
        viewModelScope.launch {
            val request = ExportResumeRequest(currentResumeId, currentVersionId, format)
            val result = exportResumeUseCase(request)
            if (result is Result.Success) {
                sendEffect(ResumeUiEffect.ExportResult(result.data))
            }
        }
    }

    private fun sendEffect(effect: ResumeUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
