package com.bangersoul.aivance.feature.jobs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.Application
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ApplicationWorkflowRepository
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.job.GetJobDetailsUseCase
import com.bangersoul.aivance.core.domain.usecase.job.ToggleJobBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface JobDetailsUiState {
    data object Loading : JobDetailsUiState
    data class Success(
        val job: JobListing,
        val isBookmarked: Boolean = false
    ) : JobDetailsUiState
    data class Error(val message: String) : JobDetailsUiState
}

sealed interface JobDetailsUiEvent {
    data object ToggleBookmark : JobDetailsUiEvent
    data object OpenUrl : JobDetailsUiEvent
    data object ApplyAndTrack : JobDetailsUiEvent
    data object FindRecruiters : JobDetailsUiEvent
    data object GenerateCoverLetter : JobDetailsUiEvent
    data object Reload : JobDetailsUiEvent
}

sealed interface JobDetailsUiEffect {
    data class ShowSnackbar(val message: String) : JobDetailsUiEffect
    data class OpenExternalUrl(val url: String) : JobDetailsUiEffect
    data class NavigateToRecruiters(val jobId: String) : JobDetailsUiEffect
    data object NavigateToCoverLetter : JobDetailsUiEffect
    data object NavigateToPipeline : JobDetailsUiEffect
}

@HiltViewModel
class JobDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getJobDetailsUseCase: GetJobDetailsUseCase,
    private val toggleJobBookmarkUseCase: ToggleJobBookmarkUseCase,
    private val jobRepository: JobRepository,
    private val applicationWorkflowRepository: ApplicationWorkflowRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private var jobId: String = savedStateHandle.get<String>("jobId") ?: ""

    private val _uiState = MutableStateFlow<JobDetailsUiState>(JobDetailsUiState.Loading)
    val uiState: StateFlow<JobDetailsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<JobDetailsUiEffect>(Channel.BUFFERED)
    val effects: Flow<JobDetailsUiEffect> = _effects.receiveAsFlow()

    init {
        // The custom navigation back stack does not seed SavedStateHandle with
        // destination arguments, so the screen supplies the real job ID via load().
        if (jobId.isNotBlank()) {
            loadJobDetails()
        }
    }

    /**
     * Loads details for [jobId]. Called by the screen on first composition and
     * whenever the destination's job ID changes, so a reused ViewModel instance
     * correctly reloads for a different job.
     */
    fun load(jobId: String) {
        this.jobId = jobId
        loadJobDetails()
    }

    fun onEvent(event: JobDetailsUiEvent) {
        when (event) {
            JobDetailsUiEvent.ToggleBookmark -> toggleBookmark()
            JobDetailsUiEvent.OpenUrl -> openUrl()
            JobDetailsUiEvent.ApplyAndTrack -> applyAndTrack()
            JobDetailsUiEvent.FindRecruiters -> findRecruiters()
            JobDetailsUiEvent.GenerateCoverLetter -> generateCoverLetter()
            JobDetailsUiEvent.Reload -> loadJobDetails()
        }
    }

    private fun loadJobDetails() {
        viewModelScope.launch {
            _uiState.value = JobDetailsUiState.Loading
            val result = getJobDetailsUseCase(jobId)
            when (result) {
                is Result.Success -> {
                    _uiState.value = JobDetailsUiState.Success(job = result.data)
                }
                is Result.Failure -> {
                    _uiState.value = JobDetailsUiState.Error(result.error.message)
                }
            }
        }
    }

    private fun toggleBookmark() {
        viewModelScope.launch {
            val state = _uiState.value as? JobDetailsUiState.Success ?: return@launch
            val result = toggleJobBookmarkUseCase(jobId)
            when (result) {
                is Result.Success -> {
                    _uiState.value = state.copy(isBookmarked = result.data)
                    _effects.send(JobDetailsUiEffect.ShowSnackbar(if (result.data) "Bookmarked" else "Bookmark removed"))
                }
                is Result.Failure -> {
                    _effects.send(JobDetailsUiEffect.ShowSnackbar(result.error.message ?: "Failed to update bookmark"))
                }
            }
        }
    }

    private fun openUrl() {
        val state = _uiState.value as? JobDetailsUiState.Success ?: return
        viewModelScope.launch {
            _effects.send(JobDetailsUiEffect.OpenExternalUrl(state.job.url))
            trackEventUseCase(TrackEventRequest("job_details_open_url"))
        }
    }

    /**
     * Apply & Track: persists the job (so the FK is valid) and creates an
     * [Application] in the SAVED stage, then routes the user to the Pipeline.
     */
    private fun applyAndTrack() {
        val state = _uiState.value as? JobDetailsUiState.Success ?: return
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("job_apply_and_track"))
            val cacheResult = jobRepository.cacheJob(state.job)
            val dbJobId = when (cacheResult) {
                is Result.Success -> cacheResult.data
                is Result.Failure -> {
                    _effects.send(JobDetailsUiEffect.ShowSnackbar(
                        cacheResult.error.message ?: "Failed to save this job"
                    ))
                    return@launch
                }
            }

            val now = System.currentTimeMillis()
            val application = Application(
                jobId = dbJobId,
                currentStageId = "SAVED",
                status = "ACTIVE",
                dateApplied = now,
                lastModified = now
            )
            val saveResult = applicationWorkflowRepository.saveApplication(application)
            if (saveResult is Result.Success) {
                _effects.send(JobDetailsUiEffect.NavigateToPipeline)
                _effects.send(JobDetailsUiEffect.ShowSnackbar("Added to Pipeline"))
            } else {
                _effects.send(JobDetailsUiEffect.ShowSnackbar(
                    (saveResult as? Result.Failure)?.error?.message ?: "Failed to add application"
                ))
            }
        }
    }

    private fun findRecruiters() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("job_find_recruiters"))
            _effects.send(JobDetailsUiEffect.NavigateToRecruiters(jobId))
        }
    }

    private fun generateCoverLetter() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("job_generate_cover_letter"))
            _effects.send(JobDetailsUiEffect.NavigateToCoverLetter)
        }
    }
}
