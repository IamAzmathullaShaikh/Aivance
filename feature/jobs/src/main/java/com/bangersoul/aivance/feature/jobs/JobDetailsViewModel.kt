package com.bangersoul.aivance.feature.jobs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.Result
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
}

sealed interface JobDetailsUiEffect {
    data class ShowSnackbar(val message: String) : JobDetailsUiEffect
    data class OpenExternalUrl(val url: String) : JobDetailsUiEffect
}

@HiltViewModel
class JobDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getJobDetailsUseCase: GetJobDetailsUseCase,
    private val toggleJobBookmarkUseCase: ToggleJobBookmarkUseCase,
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
            JobDetailsUiEvent.OpenUrl -> openUrl()
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
            if (result is Result.Success) {
                _uiState.value = state.copy(isBookmarked = result.data)
                _effects.send(JobDetailsUiEffect.ShowSnackbar(if (result.data) "Bookmarked" else "Bookmark removed"))
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
}
