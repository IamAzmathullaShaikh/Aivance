package com.bangersoul.aivance.feature.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.feature.tracker.domain.ApplicationStatus
import com.bangersoul.aivance.feature.tracker.domain.JobApplication
import com.bangersoul.aivance.feature.tracker.domain.JobTrackerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed interface TrackerUiState {
    object Loading : TrackerUiState
    data class Success(val applications: List<JobApplication>) : TrackerUiState
    data class Error(val message: String) : TrackerUiState
}

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val repository: JobTrackerRepository
) : ViewModel() {

    val uiState: StateFlow<TrackerUiState> = repository.getApplications()
        .map { applications ->
            TrackerUiState.Success(applications.sortedByDescending { it.dateApplied })
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TrackerUiState.Loading
        )

    fun addApplication(company: String, role: String, status: ApplicationStatus) {
        viewModelScope.launch {
            val newApp = JobApplication(
                company = company,
                role = role,
                status = status,
                dateApplied = Instant.now(),
                salaryRange = null,
                notes = null,
                lastModified = Instant.now()
            )
            repository.addApplication(newApp)
        }
    }

    fun deleteApplication(id: Long) {
        viewModelScope.launch {
            repository.getApplicationById(id)?.let {
                repository.deleteApplication(it)
            }
        }
    }

    fun updateStatus(id: Long, status: ApplicationStatus) {
        viewModelScope.launch {
            repository.getApplicationById(id)?.let {
                repository.updateApplication(it.copy(status = status, lastModified = Instant.now()))
            }
        }
    }
}
