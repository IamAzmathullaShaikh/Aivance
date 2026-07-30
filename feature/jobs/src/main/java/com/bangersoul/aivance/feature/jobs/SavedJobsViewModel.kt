package com.bangersoul.aivance.feature.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.job.RemoveSavedJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SearchSavedJobsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SavedJobsUiState {
    data object Loading : SavedJobsUiState
    data class Success(
        val jobs: List<JobListing> = emptyList(),
        val searchQuery: String = ""
    ) : SavedJobsUiState
    data object Empty : SavedJobsUiState
    data class Error(val message: String) : SavedJobsUiState
}

sealed interface SavedJobsUiEvent {
    data class Search(val query: String) : SavedJobsUiEvent
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
    private val searchSavedJobsUseCase: SearchSavedJobsUseCase,
    private val removeSavedJobUseCase: RemoveSavedJobUseCase,
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
            is SavedJobsUiEvent.Search -> search(event.query)
            is SavedJobsUiEvent.RemoveJob -> removeJob(event.jobId)
            is SavedJobsUiEvent.ViewDetails -> viewDetails(event.jobId)
            SavedJobsUiEvent.Refresh -> loadSavedJobs()
        }
    }

    private fun loadSavedJobs() {
        viewModelScope.launch {
            _uiState.value = SavedJobsUiState.Loading
            // SearchSavedJobsUseCase.invoke() returns Flow<PagingData<JobApplication>>
            // For now, show empty state since PagingData is consumed by Compose
            _uiState.value = SavedJobsUiState.Empty
        }
    }

    private fun search(query: String) {
        val currentState = _uiState.value
        if (currentState is SavedJobsUiState.Success) {
            _uiState.value = currentState.copy(searchQuery = query)
        }
    }

    private fun removeJob(jobId: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "saved_jobs_remove"))
            val appId = jobId.toLongOrNull() ?: return@launch
            val result = removeSavedJobUseCase(appId)
            when (result) {
                is Result.Success<*> -> {
                    _effects.send(SavedJobsUiEffect.ShowSnackbar("Job removed from saved"))
                    loadSavedJobs()
                }
                is Result.Failure -> {
                    _effects.send(SavedJobsUiEffect.ShowSnackbar(
                        result.error.message ?: "Failed to remove job"
                    ))
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
