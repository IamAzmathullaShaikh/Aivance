package com.bangersoul.aivance.feature.resume

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ResumeDetailUiState {
    data object Loading : ResumeDetailUiState
    data class Success(
        val resume: Resume? = null,
        val versions: List<ResumeVersion> = emptyList()
    ) : ResumeDetailUiState
    data class Error(val message: String) : ResumeDetailUiState
}

@HiltViewModel
class ResumeDetailViewModel @Inject constructor(
    private val resumeRepository: ResumeRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResumeDetailUiState>(ResumeDetailUiState.Loading)
    val uiState: StateFlow<ResumeDetailUiState> = _uiState.asStateFlow()

    fun load(resumeId: Long) {
        viewModelScope.launch {
            _uiState.value = ResumeDetailUiState.Loading
            trackEventUseCase(TrackEventRequest(eventName = "resume_detail_open"))

            val resumeResult = resumeRepository.getResumeById(resumeId).firstOrNull()
            val resume = when (resumeResult) {
                is Result.Success -> resumeResult.data
                is Result.Failure -> {
                    _uiState.value = ResumeDetailUiState.Error(
                        resumeResult.error.message ?: "Failed to load resume"
                    )
                    return@launch
                }
                null -> {
                    _uiState.value = ResumeDetailUiState.Error("Failed to load resume")
                    return@launch
                }
            }

            val versionsResult = resumeRepository.getVersions(resumeId).firstOrNull()
            val versions = when (versionsResult) {
                is Result.Success -> versionsResult.data
                else -> emptyList()
            }
            _uiState.value = ResumeDetailUiState.Success(resume = resume, versions = versions)
        }
    }
}
