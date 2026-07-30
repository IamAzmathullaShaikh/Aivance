package com.bangersoul.aivance.feature.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.bangersoul.aivance.core.common.enums.JobType
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.job.ApplyToJobRequest
import com.bangersoul.aivance.core.domain.usecase.job.ApplyToJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.BookmarkJobRequest
import com.bangersoul.aivance.core.domain.usecase.job.BookmarkJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.RemoveSavedJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SaveJobRequest
import com.bangersoul.aivance.core.domain.usecase.job.SaveJobUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsRequest
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsUseCase
import com.bangersoul.aivance.core.domain.usecase.job.SearchRemoteJobsRequest
import com.bangersoul.aivance.core.domain.usecase.job.SearchRemoteJobsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface JobsUiState {
    data object Loading : JobsUiState
    data class Success(
        val jobs: List<JobListing> = emptyList(),
        val query: String = "",
        val isRemoteOnly: Boolean = false,
        val selectedJobTypes: Set<JobType> = emptySet(),
        val isSearching: Boolean = false
    ) : JobsUiState
    data object Empty : JobsUiState
    data class Error(val message: String, val isOffline: Boolean = false) : JobsUiState
}

sealed interface JobsUiEvent {
    data class Search(val query: String) : JobsUiEvent
    data class ToggleRemote(val isRemote: Boolean) : JobsUiEvent
    data class SaveJob(val job: JobListing) : JobsUiEvent
    data class BookmarkJob(val company: String, val role: String) : JobsUiEvent
    data class ViewDetails(val jobId: String) : JobsUiEvent
    data class OpenApplication(val url: String) : JobsUiEvent
    data class ApplyToJob(val company: String, val role: String) : JobsUiEvent
    data object Refresh : JobsUiEvent
    data object Retry : JobsUiEvent
}

sealed interface JobsUiEffect {
    data class ShowSnackbar(val message: String) : JobsUiEffect
    data class NavigateToDetails(val jobId: String) : JobsUiEffect
    data class OpenExternalUrl(val url: String) : JobsUiEffect
}

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val searchJobsUseCase: SearchJobsUseCase,
    private val searchRemoteJobsUseCase: SearchRemoteJobsUseCase,
    private val saveJobUseCase: SaveJobUseCase,
    private val bookmarkJobUseCase: BookmarkJobUseCase,
    private val removeSavedJobUseCase: RemoveSavedJobUseCase,
    private val applyToJobUseCase: ApplyToJobUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _isRemoteOnly = MutableStateFlow(false)
    val isRemoteOnly: StateFlow<Boolean> = _isRemoteOnly.asStateFlow()

    private val _selectedJobTypes = MutableStateFlow<Set<JobType>>(emptySet())

    private val _effects = Channel<JobsUiEffect>(Channel.BUFFERED)
    val effects: Flow<JobsUiEffect> = _effects.receiveAsFlow()

    private val _uiState = MutableStateFlow<JobsUiState>(JobsUiState.Loading)
    val uiState: StateFlow<JobsUiState> = _uiState.asStateFlow()

    init {
        search()
    }

    fun onEvent(event: JobsUiEvent) {
        when (event) {
            is JobsUiEvent.Search -> {
                _query.value = event.query
                search()
            }
            is JobsUiEvent.ToggleRemote -> {
                _isRemoteOnly.value = event.isRemote
                search()
            }
            is JobsUiEvent.SaveJob -> saveJob(event.job)
            is JobsUiEvent.BookmarkJob -> bookmarkJob(event.company, event.role)
            is JobsUiEvent.ViewDetails -> viewDetails(event.jobId)
            is JobsUiEvent.OpenApplication -> openApplication(event.url)
            is JobsUiEvent.ApplyToJob -> applyToJob(event.company, event.role)
            JobsUiEvent.Refresh -> search()
            JobsUiEvent.Retry -> search()
        }
    }

    fun onQueryChange(query: String) {
        _query.value = query
    }

    fun toggleFilter(filter: String) {
        // Toggle the isRemoteOnly flag when the "Remote" filter is toggled
        if (filter.lowercase() == "remote") {
            _isRemoteOnly.value = !_isRemoteOnly.value
        }
    }

    fun addJobToTracker(job: JobListing) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "job_add_to_tracker"))
            val request = SaveJobRequest(jobListing = job)
            val result = saveJobUseCase(request)
            @Suppress("UNCHECKED_CAST")
            when {
                result.isSuccess -> sendEffect(JobsUiEffect.ShowSnackbar("Job added to tracker"))
                result.isFailure -> {
                    val msg = (result as Result.Failure).error.message ?: "Failed to add job"
                    sendEffect(JobsUiEffect.ShowSnackbar(msg))
                }
            }
        }
    }

    fun search() {
        viewModelScope.launch {
            _uiState.value = JobsUiState.Loading
            trackEventUseCase(TrackEventRequest(eventName = "job_search"))

            val queryStr = _query.value
            val isRemote = _isRemoteOnly.value

            val searchRequest = SearchJobsRequest(
                query = queryStr,
                isRemote = isRemote
            )

            val flow: Flow<PagingData<JobListing>> = if (isRemote) {
                searchRemoteJobsUseCase(SearchRemoteJobsRequest(query = queryStr))
            } else {
                searchJobsUseCase(searchRequest)
            }

            flow.catch { e ->
                _uiState.value = JobsUiState.Error(
                    message = e.message ?: "Search failed",
                    isOffline = true
                )
            }.collect { pagingData ->
                // PagingData is consumed by Compose's collectAsLazyPagingItems
                // In this ViewModel, we use the jobs from the UI state
                val currentState = _uiState.value
                if (currentState is JobsUiState.Loading) {
                    _uiState.value = JobsUiState.Success(
                        jobs = emptyList(),
                        query = queryStr,
                        isRemoteOnly = isRemote
                    )
                }
            }
        }
    }

    private fun saveJob(job: JobListing) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "job_save"))
            val request = SaveJobRequest(jobListing = job)
            val result = saveJobUseCase(request)
            @Suppress("UNCHECKED_CAST")
            when {
                result.isSuccess -> sendEffect(JobsUiEffect.ShowSnackbar("Job saved"))
                result.isFailure -> {
                    val msg = (result as Result.Failure).error.message ?: "Save failed"
                    sendEffect(JobsUiEffect.ShowSnackbar(msg))
                }
            }
        }
    }

    private fun bookmarkJob(company: String, role: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "job_bookmark"))
            val request = BookmarkJobRequest(company = company, role = role)
            val result = bookmarkJobUseCase(request)
            @Suppress("UNCHECKED_CAST")
            when {
                result.isSuccess -> sendEffect(JobsUiEffect.ShowSnackbar("Job bookmarked"))
                result.isFailure -> {
                    val msg = (result as Result.Failure).error.message ?: "Failed"
                    sendEffect(JobsUiEffect.ShowSnackbar(msg))
                }
            }
        }
    }

    private fun applyToJob(company: String, role: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "job_apply"))
            val request = ApplyToJobRequest(company = company, role = role)
            val result = applyToJobUseCase(request)
            @Suppress("UNCHECKED_CAST")
            when {
                result.isSuccess -> sendEffect(JobsUiEffect.ShowSnackbar("Application recorded"))
                result.isFailure -> {
                    val msg = (result as Result.Failure).error.message ?: "Failed"
                    sendEffect(JobsUiEffect.ShowSnackbar(msg))
                }
            }
        }
    }

    private fun viewDetails(jobId: String) {
        viewModelScope.launch { _effects.send(JobsUiEffect.NavigateToDetails(jobId)) }
    }

    private fun openApplication(url: String) {
        viewModelScope.launch {
            _effects.send(JobsUiEffect.OpenExternalUrl(url))
            trackEventUseCase(TrackEventRequest(eventName = "job_open_application"))
        }
    }

    private fun sendEffect(effect: JobsUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
