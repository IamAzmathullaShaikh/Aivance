package com.bangersoul.aivance.feature.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.enums.JobType
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsRequest
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsUseCase
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

sealed interface JobsUiState {
    data object Loading : JobsUiState
    data class Success(
        val jobs: List<JobListing> = emptyList(),
        val filter: JobSearchFilter = JobSearchFilter(),
        val isSearching: Boolean = false
    ) : JobsUiState
    data class Error(val message: String) : JobsUiState
}

sealed interface JobsUiEvent {
    data class Search(val query: String) : JobsUiEvent
    data class UpdateFilter(val filter: JobSearchFilter) : JobsUiEvent
    data class ToggleBookmark(val jobId: String) : JobsUiEvent
    data class ViewDetails(val jobId: String) : JobsUiEvent
    data object Refresh : JobsUiEvent
}

sealed interface JobsUiEffect {
    data class ShowSnackbar(val message: String) : JobsUiEffect
    data class NavigateToDetails(val jobId: String) : JobsUiEffect
}

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val searchJobsUseCase: SearchJobsUseCase,
    private val toggleJobBookmarkUseCase: ToggleJobBookmarkUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<JobsUiState>(JobsUiState.Success())
    val uiState: StateFlow<JobsUiState> = _uiState.asStateFlow()

    private val _effects = Channel<JobsUiEffect>(Channel.BUFFERED)
    val effects: Flow<JobsUiEffect> = _effects.receiveAsFlow()

    fun onEvent(event: JobsUiEvent) {
        when (event) {
            is JobsUiEvent.Search -> search(event.query)
            is JobsUiEvent.UpdateFilter -> updateFilter(event.filter)
            is JobsUiEvent.ToggleBookmark -> toggleBookmark(event.jobId)
            is JobsUiEvent.ViewDetails -> viewModelScope.launch { _effects.send(JobsUiEffect.NavigateToDetails(event.jobId)) }
            JobsUiEvent.Refresh -> search()
        }
    }

    private fun search(query: String? = null) {
        val current = _uiState.value as? JobsUiState.Success ?: return
        val newFilter = query?.let { current.filter.copy(query = it) } ?: current.filter

        viewModelScope.launch {
            _uiState.value = JobsUiState.Loading
            trackEventUseCase(TrackEventRequest("job_discovery_search"))

            val result = searchJobsUseCase(SearchJobsRequest(filter = newFilter))
            when (result) {
                is Result.Success -> {
                    _uiState.value = JobsUiState.Success(jobs = result.data, filter = newFilter)
                }
                is Result.Failure -> {
                    _uiState.value = JobsUiState.Error(result.error.message)
                }
            }
        }
    }

    private fun updateFilter(filter: JobSearchFilter) {
        val current = _uiState.value as? JobsUiState.Success ?: return
        _uiState.value = current.copy(filter = filter)
        search()
    }

    private fun toggleBookmark(jobId: String) {
        viewModelScope.launch {
            val result = toggleJobBookmarkUseCase(jobId)
            if (result is Result.Success) {
                val message = if (result.data) "Job bookmarked" else "Bookmark removed"
                _effects.send(JobsUiEffect.ShowSnackbar(message))
            }
        }
    }
}
