package com.bangersoul.aivance.feature.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
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

sealed interface SavedJobsUiState {
    data object Loading : SavedJobsUiState
    data class Success(
        val jobs: List<JobListing> = emptyList()
    ) : SavedJobsUiState
    data object Empty : SavedJobsUiState
    data class Error(val message: String) : SavedJobsUiState
}

sealed interface SavedJobsUiEvent {
    data class RemoveJob(val jobId: String) : SavedJobsUiEvent
    data class ViewDetails(val jobId: String) : SavedJobsUiEvent
    data object Refresh : SavedJobsUiEvent
}

sealed interface SavedJobsUiEffect {
    data class ShowSnackbar(val message: String) : SavedJobsUiEffect
    data class NavigateToDetails(val jobId: String) : SavedJobsUiEffect
}

@HiltViewModel
class SavedJobsViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val toggleJobBookmarkUseCase: ToggleJobBookmarkUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SavedJobsUiState>(SavedJobsUiState.Loading)
    val uiState: StateFlow<SavedJobsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<SavedJobsUiEffect>(Channel.BUFFERED)
    val effects: Flow<SavedJobsUiEffect> = _effects.receiveAsFlow()

    init {
        loadSavedJobs()
    }

    fun onEvent(event: SavedJobsUiEvent) {
        when (event) {
            is SavedJobsUiEvent.RemoveJob -> removeJob(event.jobId)
            is SavedJobsUiEvent.ViewDetails -> viewDetails(event.jobId)
            SavedJobsUiEvent.Refresh -> loadSavedJobs()
        }
    }

    private fun loadSavedJobs() {
        viewModelScope.launch {
            _uiState.value = SavedJobsUiState.Loading
            jobRepository.getSavedJobs().collect { result ->
                when (result) {
                    is Result.Success -> {
                        if (result.data.isEmpty()) {
                            _uiState.value = SavedJobsUiState.Empty
                        } else {
                            _uiState.value = SavedJobsUiState.Success(result.data)
                        }
                    }
                    is Result.Failure -> {
                        _uiState.value = SavedJobsUiState.Error(result.error.message)
                    }
                }
            }
        }
    }

    private fun removeJob(jobId: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "saved_jobs_remove"))
            val result = toggleJobBookmarkUseCase(jobId)
            when (result) {
                is Result.Success -> {
                    _effects.send(SavedJobsUiEffect.ShowSnackbar("Job removed from saved"))
                    loadSavedJobs()
                }
                is Result.Failure -> {
                    _effects.send(SavedJobsUiEffect.ShowSnackbar(result.error.message ?: "Failed to remove job"))
                }
            }
        }
    }

    private fun viewDetails(jobId: String) {
        viewModelScope.launch {
            _effects.send(SavedJobsUiEffect.NavigateToDetails(jobId))
        }
    }
}
