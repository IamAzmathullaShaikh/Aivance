package com.bangersoul.aivance.feature.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.Application
import com.bangersoul.aivance.core.common.model.ApplicationStage
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ApplicationWorkflowRepository
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.workflow.WorkflowEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TrackerUiState {
    data object Loading : TrackerUiState
    data class Success(
        val applications: List<Application> = emptyList(),
        val stages: List<ApplicationStage> = emptyList(),
        val selectedApplicationId: Long? = null
    ) : TrackerUiState
    data class Error(val message: String) : TrackerUiState
}

sealed interface TrackerUiEvent {
    data class UpdateStage(val applicationId: Long, val stageId: String) : TrackerUiEvent
    data class DeleteApplication(val id: Long) : TrackerUiEvent
    data class SelectApplication(val applicationId: Long) : TrackerUiEvent
    data object CloseApplication : TrackerUiEvent
    data class UpdateNotes(val applicationId: Long, val notes: String) : TrackerUiEvent
    /** Manually add a job application (company + role) to the selected stage. */
    data class AddApplication(val company: String, val role: String, val stageId: String) : TrackerUiEvent
    data object Refresh : TrackerUiEvent
}

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val repository: ApplicationWorkflowRepository,
    private val workflowEngine: WorkflowEngine,
    private val trackEventUseCase: TrackEventUseCase,
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrackerUiState>(TrackerUiState.Loading)
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private val _effects = Channel<String>(Channel.BUFFERED)
    val effects: Flow<String> = _effects.receiveAsFlow()

    init {
        loadData()
    }

    fun onEvent(event: TrackerUiEvent) {
        when (event) {
            is TrackerUiEvent.UpdateStage -> updateStage(event.applicationId, event.stageId)
            is TrackerUiEvent.DeleteApplication -> deleteApplication(event.id)
            is TrackerUiEvent.SelectApplication -> selectApplication(event.applicationId)
            TrackerUiEvent.CloseApplication -> closeApplication()
            is TrackerUiEvent.UpdateNotes -> updateNotes(event.applicationId, event.notes)
            is TrackerUiEvent.AddApplication -> addApplication(event.company, event.role, event.stageId)
            TrackerUiEvent.Refresh -> loadData()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = TrackerUiState.Loading

            combine(
                repository.getApplications(),
                repository.getStages()
            ) { appsRes, stagesRes ->
                if (appsRes is Result.Success && stagesRes is Result.Success) {
                    TrackerUiState.Success(appsRes.data, stagesRes.data)
                } else {
                    TrackerUiState.Error("Failed to load pipeline")
                }
            }.collect { state ->
                // Preserve the selected application across Room re-emissions
                // (e.g. while the user edits notes) so the detail sheet stays
                // open instead of dismissing on every DB write.
                val previous = _uiState.value as? TrackerUiState.Success
                _uiState.value = if (state is TrackerUiState.Success) {
                    state.copy(selectedApplicationId = previous?.selectedApplicationId)
                } else {
                    state
                }
            }
        }
    }

    private fun updateStage(applicationId: Long, stageId: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("tracker_stage_update"))
            repository.getApplicationById(applicationId).firstOrNull()?.let { res ->
                if (res is Result.Success) {
                    // Route through the WorkflowEngine so the transition is
                    // validated, timeline events are recorded, and analytics
                    // snapshots are refreshed (dashboard career score updates).
                    // Only surface failures — a successful move is already visible
                    // in the board, so a snackbar on every drag would be noise.
                    val transition = workflowEngine.transitionTo(res.data, stageId)
                    if (transition is Result.Failure) {
                        _effects.send(
                            transition.error.message ?: "Failed to update stage"
                        )
                    }
                    loadData()
                }
            }
        }
    }

    private fun deleteApplication(id: Long) {
        viewModelScope.launch {
            repository.deleteApplication(id)
            closeApplication()
            loadData()
        }
    }

    private fun selectApplication(applicationId: Long) {
        val current = _uiState.value as? TrackerUiState.Success ?: return
        _uiState.value = current.copy(selectedApplicationId = applicationId)
    }

    private fun closeApplication() {
        val current = _uiState.value as? TrackerUiState.Success ?: return
        _uiState.value = current.copy(selectedApplicationId = null)
    }

    private fun updateNotes(applicationId: Long, notes: String) {
        viewModelScope.launch {
            val result = repository.updateNotes(applicationId, notes)
            if (result is Result.Failure) {
                _effects.send(result.error.message ?: "Failed to save notes")
            }
        }
    }

    /**
     * Manually adds an application to the pipeline. The Application row has a
     * non-null [Application.jobId] foreign key into the jobs table, so the
     * synthetic job is cached first (reusing an existing row for the same URL)
     * and the returned DB id is used as the FK.
     */
    private fun addApplication(company: String, role: String, stageId: String) {
        if (company.isBlank() || role.isBlank()) {
            viewModelScope.launch { _effects.send("Enter a company and role to add an application.") }
            return
        }
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("tracker_manual_add"))
            val now = System.currentTimeMillis()
            val job = JobListing(
                id = "manual-${now}",
                title = role.trim(),
                company = company.trim(),
                description = "",
                url = "manual://${now}",
                sourceProvider = "manual"
            )
            val cacheResult = jobRepository.cacheJob(job)
            when (cacheResult) {
                is Result.Success -> {
                    val application = Application(
                        jobId = cacheResult.data,
                        currentStageId = stageId,
                        status = "ACTIVE",
                        dateApplied = now,
                        lastModified = now
                    )
                    val saveResult = repository.saveApplication(application)
                    if (saveResult is Result.Success) {
                        repository.addTimelineEvent(
                            com.bangersoul.aivance.core.common.model.TimelineEvent(
                                applicationId = saveResult.data,
                                eventType = "MANUAL_ADD",
                                title = "Added to pipeline",
                                description = "${role.trim()} at ${company.trim()} added manually",
                                timestamp = now
                            )
                        )
                        _effects.send("Added ${role.trim()} at ${company.trim()}")
                    } else {
                        _effects.send(
                            (saveResult as? Result.Failure)?.error?.message ?: "Failed to add application"
                        )
                    }
                }
                is Result.Failure -> {
                    _effects.send(cacheResult.error.message ?: "Failed to prepare job")
                }
            }
            loadData()
        }
    }
}
