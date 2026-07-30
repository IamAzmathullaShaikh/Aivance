package com.bangersoul.aivance.feature.jobs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.job.ApplyToJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.BookmarkJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.GetJobDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface JobDetailsUiState {
    data object Loading : JobDetailsUiState
    data class Success(
        val job: JobListing,
        val isBookmarked: Boolean = false,
        val isApplying: Boolean = false
    ) : JobDetailsUiState
    data class Error(val message: String) : JobDetailsUiState
}

sealed interface JobDetailsUiEvent {
    data object ToggleBookmark : JobDetailsUiEvent
    data object Apply : JobDetailsUiEvent
    data object OpenUrl : JobDetailsUiEvent
    data object Share : JobDetailsUiEvent
}

sealed interface JobDetailsUiEffect {
    data class ShowSnackbar(val message: String) : JobDetailsUiEffect
    data class OpenExternalUrl(val url: String) : JobDetailsUiEffect
    data class ShareJob(val title: String, val url: String) : JobDetailsUiEffect
}

@HiltViewModel
class JobDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getJobDetailsUseCase: GetJobDetailsUseCase,
    private val bookmarkJobUseCase: BookmarkJobUseCase,
    private val applyToJobUseCase: ApplyToJobUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val jobId: String = savedStateHandle.get<String>("jobId") ?: ""

    private val _uiState = MutableStateFlow<JobDetailsUiState>(JobDetailsUiState.Loading)
    val uiState: StateFlow<JobDetailsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<JobDetailsUiEffect>(Channel.BUFFERED)
    val effects: Flow<JobDetailsUiEffect> = _effects.receiveAsFlow()

    init {
        loadJobDetails()
    }

    fun onEvent(event: JobDetailsUiEvent) {
        when (event) {
            JobDetailsUiEvent.ToggleBookmark -> toggleBookmark()
            JobDetailsUiEvent.Apply -> apply()
            JobDetailsUiEvent.OpenUrl -> openUrl()
            JobDetailsUiEvent.Share -> share()
        }
    }

    private fun loadJobDetails() {
        if (jobId.isBlank()) {
            _uiState.value = JobDetailsUiState.Error("Job ID not provided")
            return
        }
        viewModelScope.launch {
            getJobDetailsUseCase(jobId)
                .onStart { _uiState.value = JobDetailsUiState.Loading }
                .catch { e -> _uiState.value = JobDetailsUiState.Error(e.message ?: "Failed to load") }
                .collect { result ->
                    when (result) {
                        is CoreResult.Success -> {
                            _uiState.value = JobDetailsUiState.Success(job = result.data)
                        }
                        is CoreResult.Failure -> {
                            _uiState.value = JobDetailsUiState.Error(result.error.message ?: "Failed to load")
                        }
                    }
                }
        }
    }

    private fun toggleBookmark() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is JobDetailsUiState.Success) {
                trackEventUseCase(TrackEventRequest(eventName = "job_details_toggle_bookmark"))
                val result = bookmarkJobUseCase(state.job.id).firstOrNull()
                when (result) {
                    is CoreResult.Success -> {
                        _uiState.value = state.copy(isBookmarked = result.data)
                        _effects.send(JobDetailsUiEffect.ShowSnackbar(
                            if (result.data) "Job saved" else "Bookmark removed"
                        ))
                    }
                    is CoreResult.Failure -> {
                        _effects.send(JobDetailsUiEffect.ShowSnackbar(
                            result.error.message ?: "Failed to update bookmark"
                        ))
                    }
                    null -> { /* noop */ }
                }
            }
        }
    }

    private fun apply() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state is JobDetailsUiState.Success) {
                trackEventUseCase(TrackEventRequest(eventName = "job_details_apply"))
                _uiState.value = state.copy(isApplying = true)
                val result = applyToJobUseCase(state.job.id).firstOrNull()
                when (result) {
                    is CoreResult.Success -> {
                        _uiState.value = state.copy(isApplying = false)
                        _effects.send(JobDetailsUiEffect.ShowSnackbar("Application recorded"))
                    }
                    is CoreResult.Failure -> {
                        _uiState.value = state.copy(isApplying = false)
                        _effects.send(JobDetailsUiEffect.ShowSnackbar(
                            result.error.message ?: "Failed to apply"
                        ))
                    }
                    null -> {
                        _uiState.value = state.copy(isApplying = false)
                    }
                }
            }
        }
    }

    private fun openUrl() {
        val state = _uiState.value
        if (state is JobDetailsUiState.Success) {
            viewModelScope.launch {
                _effects.send(JobDetailsUiEffect.OpenExternalUrl(state.job.url))
            }
        }
    }

    private fun share() {
        val state = _uiState.value
        if (state is JobDetailsUiState.Success) {
            viewModelScope.launch {
                _effects.send(JobDetailsUiEffect.ShareJob(state.job.title, state.job.url))
            }
        }
    }
}
