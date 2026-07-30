package com.bangersoul.aivance.feature.resume

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.CoreResult
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
import com.bangersoul.aivance.core.domain.usecase.resume.ImportResumeRequest
import com.bangersoul.aivance.core.domain.usecase.resume.ImportResumeUseCase
import com.bangersoul.aivance.core.domain.usecase.resume.ResumeSummaryRequest
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

sealed interface ResumeUiState {
    data object Idle : ResumeUiState
    data object Importing : ResumeUiState
    data object Parsing : ResumeUiState
    data object Analyzing : ResumeUiState
    data class Success(
        val summary: String? = null,
        val analysisResult: com.bangersoul.aivance.feature.resume.domain.model.ResumeAnalysis? = null,
        val atsScore: Int? = null,
        val resumeId: Long = 0L
    ) : ResumeUiState
    data class Error(val message: String) : ResumeUiState
    data class AnalysisSuccess(
        val analysis: com.bangersoul.aivance.feature.resume.domain.model.ResumeAnalysis
    ) : ResumeUiState
}

sealed interface ResumeUiEvent {
    data class ImportResume(val fileName: String, val uri: String, val rawText: String) : ResumeUiEvent
    data class AnalyzeResume(val resumeId: Long, val jobDescription: String) : ResumeUiEvent
    data class CalculateATSScore(val resumeId: Long, val jobDescription: String) : ResumeUiEvent
    data class ImproveResume(val resumeId: Long, val jobDescription: String) : ResumeUiEvent
    data class GenerateSummary(val resumeId: Long) : ResumeUiEvent
    data class ExportResume(val resumeId: Long) : ResumeUiEvent
    data object Reset : ResumeUiEvent
    data object Retry : ResumeUiEvent
}

sealed interface ResumeUiEffect {
    data class ShowSnackbar(val message: String) : ResumeUiEffect
    data class ExportResult(val path: String) : ResumeUiEffect
    data class NavigateToCoverLetter(val resumeId: Long, val jobDescription: String) : ResumeUiEffect
}

@HiltViewModel
class ResumeViewModel @Inject constructor(
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

    private val _resumeText = MutableStateFlow("")
    val resumeText: StateFlow<String> = _resumeText.asStateFlow()

    private val _jobDescription = MutableStateFlow("")
    val jobDescription: StateFlow<String> = _jobDescription.asStateFlow()

    private val _resumeId = MutableStateFlow(0L)

    private val _effects = Channel<ResumeUiEffect>(Channel.BUFFERED)
    val effects: Flow<ResumeUiEffect> = _effects.receiveAsFlow()

    fun onEvent(event: ResumeUiEvent) {
        when (event) {
            is ResumeUiEvent.ImportResume -> importResume(event.fileName, event.uri, event.rawText)
            is ResumeUiEvent.AnalyzeResume -> analyzeResume(event.resumeId, event.jobDescription)
            is ResumeUiEvent.CalculateATSScore -> calculateATSScore(event.resumeId, event.jobDescription)
            is ResumeUiEvent.ImproveResume -> improveResume(event.resumeId, event.jobDescription)
            is ResumeUiEvent.GenerateSummary -> generateSummary(event.resumeId)
            is ResumeUiEvent.ExportResume -> exportResume(event.resumeId)
            ResumeUiEvent.Reset -> reset()
            ResumeUiEvent.Retry -> retry()
        }
    }

    private fun importResume(fileName: String, uri: String, rawText: String) {
        viewModelScope.launch {
            _uiState.value = ResumeUiState.Importing
            trackEventUseCase(TrackEventRequest(eventName = "resume_import"))

            val request = ImportResumeRequest(fileName = fileName, fileUri = uri, rawText = rawText)
            val result = importResumeUseCase(request)
            when (result) {
                is CoreResult.Success -> {
                    _resumeId.value = result.data.resumeId
                    _resumeText.value = rawText
                    _uiState.value = ResumeUiState.Success(summary = "Imported", resumeId = result.data.resumeId)
                    sendEffect(ResumeUiEffect.ShowSnackbar("Resume imported"))
                }
                is CoreResult.Failure -> {
                    _uiState.value = ResumeUiState.Error(result.error.message ?: "Import failed")
                }
            }
        }
    }

    private fun analyzeResume(resumeId: Long, jobDescription: String) {
        if (jobDescription.isBlank()) {
            _uiState.value = ResumeUiState.Error("Job description cannot be empty")
            return
        }
        if (resumeId <= 0) {
            _uiState.value = ResumeUiState.Error("No resume selected. Import resume first.")
            return
        }
        viewModelScope.launch {
            _uiState.value = ResumeUiState.Analyzing
            trackEventUseCase(TrackEventRequest(eventName = "resume_analyze"))

            val request = AnalyseResumeRequest(resumeId = resumeId, jobDescription = jobDescription)
            val result = analyseResumeUseCase(request)
            when (result) {
                is CoreResult.Success -> {
                    val analysis = result.data
                    _uiState.value = ResumeUiState.AnalysisSuccess(
                        analysis = com.bangersoul.aivance.feature.resume.domain.model.ResumeAnalysis(
                            overallScore = analysis.overallScore,
                            matchingKeywords = analysis.matchingKeywords,
                            missingKeywords = analysis.missingKeywords,
                            suggestions = analysis.suggestions,
                            matchSummary = analysis.matchSummary
                        )
                    )
                }
                is CoreResult.Failure -> {
                    _uiState.value = ResumeUiState.Error(result.error.message ?: "Analysis failed")
                }
            }
        }
    }

    private fun calculateATSScore(resumeId: Long, jobDescription: String) {
        viewModelScope.launch {
            val request = AtsScoreRequest(resumeId = resumeId, jobDescription = jobDescription)
            val result = calculateATSScoreUseCase(request)
            when (result) {
                is CoreResult.Success -> {
                    _uiState.value = ResumeUiState.Success(atsScore = result.data.atsResult.score)
                }
                is CoreResult.Failure -> {
                    _uiState.value = ResumeUiState.Error(result.error.message ?: "ATS score failed")
                }
            }
        }
    }

    private fun improveResume(resumeId: Long, jobDescription: String) {
        viewModelScope.launch {
            val request = ImproveResumeRequest(resumeId = resumeId, jobDescription = jobDescription)
            val result = improveResumeUseCase(request)
            when (result) {
                is CoreResult.Success -> {
                    _resumeText.value = result.data.improvedResume.rawText
                    _uiState.value = ResumeUiState.Success(summary = "Resume improved", resumeId = resumeId)
                    sendEffect(ResumeUiEffect.ShowSnackbar("Resume improved"))
                }
                is CoreResult.Failure -> {
                    _uiState.value = ResumeUiState.Error(result.error.message ?: "Improvement failed")
                }
            }
        }
    }

    private fun generateSummary(resumeId: Long) {
        viewModelScope.launch {
            val request = ResumeSummaryRequest(resumeId = resumeId)
            val result = generateResumeSummaryUseCase(request)
            when (result) {
                is CoreResult.Success -> {
                    _uiState.value = ResumeUiState.Success(summary = result.data, resumeId = resumeId)
                }
                is CoreResult.Failure -> {
                    _uiState.value = ResumeUiState.Error(result.error.message ?: "Summary failed")
                }
            }
        }
    }

    private fun exportResume(resumeId: Long) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "resume_export"))
            val request = ExportResumeRequest(resumeId = resumeId, format = ExportFormat.TXT)
            val result = exportResumeUseCase(request)
            when (result) {
                is CoreResult.Success -> {
                    sendEffect(ResumeUiEffect.ExportResult(result.data))
                    sendEffect(ResumeUiEffect.ShowSnackbar("Exported"))
                }
                is CoreResult.Failure -> {
                    sendEffect(ResumeUiEffect.ShowSnackbar(result.error.message ?: "Export failed"))
                }
            }
        }
    }

    fun updateResumeText(text: String) { _resumeText.value = text }
    fun updateJobDescription(text: String) { _jobDescription.value = text }

    private fun reset() {
        _uiState.value = ResumeUiState.Idle
        _resumeText.value = ""
        _jobDescription.value = ""
        _resumeId.value = 0L
    }

    private fun retry() {
        val rid = _resumeId.value
        val desc = _jobDescription.value
        if (rid > 0 && desc.isNotBlank()) analyzeResume(rid, desc)
    }

    private fun sendEffect(effect: ResumeUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
