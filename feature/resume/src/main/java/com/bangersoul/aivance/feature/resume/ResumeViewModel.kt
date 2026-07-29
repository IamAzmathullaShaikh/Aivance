package com.bangersoul.aivance.feature.resume

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.feature.resume.domain.model.ResumeAnalysis
import com.bangersoul.aivance.feature.resume.domain.repository.ResumeRepository
import com.bangersoul.aivance.feature.tracker.domain.ApplicationStatus
import com.bangersoul.aivance.feature.tracker.domain.JobApplication
import com.bangersoul.aivance.feature.tracker.domain.JobTrackerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed interface ResumeUiState {
    data object Idle : ResumeUiState
    data object Analyzing : ResumeUiState
    data class Success(val analysis: ResumeAnalysis) : ResumeUiState
    data class Error(val message: String) : ResumeUiState
}

@HiltViewModel
class ResumeViewModel @Inject constructor(
    private val repository: ResumeRepository,
    private val trackerRepository: JobTrackerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResumeUiState>(ResumeUiState.Idle)
    val uiState: StateFlow<ResumeUiState> = _uiState.asStateFlow()

    private val _resumeText = MutableStateFlow("")
    val resumeText: StateFlow<String> = _resumeText.asStateFlow()

    private val _jobDescription = MutableStateFlow("")
    val jobDescription: StateFlow<String> = _jobDescription.asStateFlow()

    fun updateResumeText(text: String) {
        _resumeText.value = text
    }

    fun updateJobDescription(text: String) {
        _jobDescription.value = text
    }

    fun analyzeResume(resumeText: String, jobDescription: String) {
        if (resumeText.isBlank() || jobDescription.isBlank()) {
            _uiState.value = ResumeUiState.Error("Resume and Job Description cannot be empty")
            return
        }

        viewModelScope.launch {
            repository.analyzeResume(resumeText, jobDescription)
                .onStart { _uiState.value = ResumeUiState.Analyzing }
                .catch { e -> _uiState.value = ResumeUiState.Error(e.message ?: "An unknown error occurred") }
                .collect { analysis ->
                    _uiState.value = ResumeUiState.Success(analysis)
                }
        }
    }

    fun addJobToTracker(company: String, role: String) {
        viewModelScope.launch {
            val application = JobApplication(
                company = company,
                role = role,
                status = ApplicationStatus.APPLIED,
                dateApplied = Instant.now(),
                salaryRange = null,
                notes = "Added from Resume Analysis",
                lastModified = Instant.now()
            )
            trackerRepository.addApplication(application)
        }
    }

    fun saveResult(resumeName: String) {
        val currentState = _uiState.value
        if (currentState is ResumeUiState.Success) {
            viewModelScope.launch {
                try {
                    repository.saveAnalysis(currentState.analysis, resumeName)
                    // Maybe show a toast or change state to indicate success
                } catch (e: Exception) {
                    _uiState.value = ResumeUiState.Error("Failed to save result: ${e.message}")
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = ResumeUiState.Idle
    }
}
