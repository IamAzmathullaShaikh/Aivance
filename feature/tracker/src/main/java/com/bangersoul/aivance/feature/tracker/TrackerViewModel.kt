package com.bangersoul.aivance.feature.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.feature.tracker.domain.ApplicationStatus
import com.bangersoul.aivance.feature.tracker.domain.JobApplication
import com.bangersoul.aivance.feature.tracker.domain.JobTrackerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed interface TrackerUiState {
    data object Loading : TrackerUiState
    data class Success(
        val applications: List<JobApplication> = emptyList(),
        val activeFilter: ApplicationStatus? = null,
        val sortedBy: SortField = SortField.DATE_DESC,
        val isRefreshing: Boolean = false
    ) : TrackerUiState
    data object Empty : TrackerUiState
    data class Error(val message: String) : TrackerUiState
}

enum class SortField {
    DATE_DESC, DATE_ASC, COMPANY_ASC
}

sealed interface TrackerUiEvent {
    data class AddApplication(val company: String, val role: String, val status: ApplicationStatus) : TrackerUiEvent
    data class UpdateStatus(val id: Long, val status: ApplicationStatus) : TrackerUiEvent
    data class UpdateNotes(val id: Long, val notes: String) : TrackerUiEvent
    data class DeleteApplication(val id: Long) : TrackerUiEvent
    data class FilterByStatus(val status: ApplicationStatus?) : TrackerUiEvent
    data class SortBy(val field: SortField) : TrackerUiEvent
    data object Refresh : TrackerUiEvent
    data object Retry : TrackerUiEvent
}

sealed interface TrackerUiEffect {
    data class ShowSnackbar(val message: String) : TrackerUiEffect
    data class UndoDelete(val application: JobApplication) : TrackerUiEffect
}

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val repository: JobTrackerRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _activeFilter = MutableStateFlow<ApplicationStatus?>(null)
    private val _sortField = MutableStateFlow(SortField.DATE_DESC)
    private val _effects = Channel<TrackerUiEffect>(Channel.BUFFERED)
    val effects: Flow<TrackerUiEffect> = _effects.receiveAsFlow()

    // Public methods for backward compatibility with existing TrackerScreen
    fun addApplication(company: String, role: String, status: ApplicationStatus) {
        onEvent(TrackerUiEvent.AddApplication(company, role, status))
    }

    fun deleteApplication(id: Long) {
        onEvent(TrackerUiEvent.DeleteApplication(id))
    }

    fun updateStatus(id: Long, status: ApplicationStatus) {
        onEvent(TrackerUiEvent.UpdateStatus(id, status))
    }

    val uiState: StateFlow<TrackerUiState> = repository.getApplications()
        .map { applications ->
            if (applications.isEmpty()) { TrackerUiState.Empty }
            else {
                val filtered = _activeFilter.value?.let { filter ->
                    applications.filter { it.status == filter }
                } ?: applications
                val sorted = when (_sortField.value) {
                    SortField.DATE_DESC -> filtered.sortedByDescending { it.dateApplied }
                    SortField.DATE_ASC -> filtered.sortedBy { it.dateApplied }
                    SortField.COMPANY_ASC -> filtered.sortedBy { it.company }
                }
                TrackerUiState.Success(applications = sorted, activeFilter = _activeFilter.value, sortedBy = _sortField.value)
            }
        }
        .catch<TrackerUiState> { e -> emit(TrackerUiState.Error(e.message ?: "Failed to load")) }
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = TrackerUiState.Loading)

    fun onEvent(event: TrackerUiEvent) {
        when (event) {
            is TrackerUiEvent.AddApplication -> addApplicationInternal(event.company, event.role, event.status)
            is TrackerUiEvent.UpdateStatus -> updateStatusInternal(event.id, event.status)
            is TrackerUiEvent.UpdateNotes -> updateNotes(event.id, event.notes)
            is TrackerUiEvent.DeleteApplication -> deleteApplicationInternal(event.id)
            is TrackerUiEvent.FilterByStatus -> _activeFilter.value = event.status
            is TrackerUiEvent.SortBy -> _sortField.value = event.field
            TrackerUiEvent.Refresh -> refresh()
            TrackerUiEvent.Retry -> refresh()
        }
    }

    private fun addApplicationInternal(company: String, role: String, status: ApplicationStatus) {
        if (company.isBlank() || role.isBlank()) {
            viewModelScope.launch { _effects.send(TrackerUiEffect.ShowSnackbar("Company and role are required")) }
            return
        }
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "tracker_add_application"))
            val newApp = JobApplication(
                company = company, role = role, status = status,
                dateApplied = Instant.now(), salaryRange = null, notes = null, lastModified = Instant.now()
            )
            repository.addApplication(newApp)
            _effects.send(TrackerUiEffect.ShowSnackbar("Application added"))
        }
    }

    private fun updateStatusInternal(id: Long, status: ApplicationStatus) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "tracker_update_status"))
            repository.getApplicationById(id)?.let { app ->
                val updated = app.copy(status = status, lastModified = Instant.now())
                repository.updateApplication(updated)
                _effects.send(TrackerUiEffect.ShowSnackbar("Status updated"))
            }
        }
    }

    private fun updateNotes(id: Long, notes: String) {
        viewModelScope.launch {
            repository.getApplicationById(id)?.let { app ->
                repository.updateApplication(app.copy(notes = notes, lastModified = Instant.now()))
                _effects.send(TrackerUiEffect.ShowSnackbar("Notes updated"))
            }
        }
    }

    private fun deleteApplicationInternal(id: Long) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "tracker_delete"))
            repository.getApplicationById(id)?.let { app ->
                repository.deleteApplication(app)
                _effects.send(TrackerUiEffect.ShowSnackbar("Application deleted"))
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch { trackEventUseCase(TrackEventRequest(eventName = "tracker_refresh")) }
    }
}
