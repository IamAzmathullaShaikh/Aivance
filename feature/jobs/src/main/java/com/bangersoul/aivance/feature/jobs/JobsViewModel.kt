package com.bangersoul.aivance.feature.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.feature.jobs.domain.JobListing
import com.bangersoul.aivance.feature.jobs.domain.JobSearchRepository
import com.bangersoul.aivance.feature.tracker.domain.ApplicationStatus
import com.bangersoul.aivance.feature.tracker.domain.JobApplication
import com.bangersoul.aivance.feature.tracker.domain.JobTrackerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val repository: JobSearchRepository,
    private val trackerRepository: JobTrackerRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filters = MutableStateFlow(setOf<String>())
    val filters: StateFlow<Set<String>> = _filters.asStateFlow()

    private val _uiState = MutableStateFlow<JobsUiState>(JobsUiState.Loading)
    val uiState: StateFlow<JobsUiState> = _uiState.asStateFlow()

    init {
        search()
    }

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        search()
    }

    fun toggleFilter(filter: String) {
        val currentFilters = _filters.value.toMutableSet()
        if (currentFilters.contains(filter)) {
            currentFilters.remove(filter)
        } else {
            currentFilters.add(filter)
        }
        _filters.value = currentFilters
        search()
    }

    fun addJobToTracker(job: JobListing) {
        viewModelScope.launch {
            val application = JobApplication(
                company = job.company,
                role = job.title,
                status = ApplicationStatus.APPLIED,
                dateApplied = Instant.now(),
                salaryRange = job.salary,
                notes = job.description,
                lastModified = Instant.now()
            )
            trackerRepository.addApplication(application)
        }
    }

    fun search() {
        viewModelScope.launch {
            _uiState.value = JobsUiState.Loading
            try {
                val results = repository.searchJobs(_query.value, _filters.value.toList())
                _uiState.value = JobsUiState.Success(results)
            } catch (e: Exception) {
                _uiState.value = JobsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed interface JobsUiState {
    data object Loading : JobsUiState
    data class Success(val jobs: List<JobListing>) : JobsUiState
    data class Error(val message: String) : JobsUiState
}
