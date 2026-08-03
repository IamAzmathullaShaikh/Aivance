package com.bangersoul.aivance.feature.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.Application
import com.bangersoul.aivance.core.common.model.ApplicationStage
import com.bangersoul.aivance.core.common.model.CareerState
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.engine.CareerStateEngine
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
        val selectedApplicationId: Long? = null,
        val careerState: CareerState? = null,
        val pipelineMetrics: PipelineMetrics = PipelineMetrics()
    ) : TrackerUiState
    data class Error(val message: String) : TrackerUiState
}

data class PipelineMetrics(
    val activeCount: Int = 0,
    val interviewRate: Int = 0,
    val offerRate: Int = 0
)

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
    private val careerStateEngine: CareerStateEngine,
    private val trackEventUseCase: TrackEventUseCase,
    private val jobRepository: JobRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrackerUiState>(TrackerUiState.Loading)
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private val _effects = Channel<String>(Channel.BUFFERED)
    val effects: Flow<String> = _effects.receiveAsFlow()

    private var loadJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            combine(
                repository.getApplications(),
                repository.getStages(),
                careerStateEngine.state
            ) { appsRes, stagesRes, careerState ->
                if (appsRes is Result.Success && stagesRes is Result.Success) {
                    val apps = appsRes.data
                    TrackerUiState.Success(
                        applications = apps,
                        stages = stagesRes.data,
                        careerState = careerState,
                        pipelineMetrics = calculateMetrics(apps)
                    )
                } else {
                    TrackerUiState.Error("Failed to load pipeline")
                }
            }.collect { state ->
                val previous = _uiState.value as? TrackerUiState.Success
                _uiState.value = if (state is TrackerUiState.Success) {
                    state.copy(selectedApplicationId = previous?.selectedApplicationId)
                } else {
                    state
                }
            }
        }
    }

    private fun calculateMetrics(apps: List<Application>): PipelineMetrics {
        val active = apps.filter { it.status == "ACTIVE" }
        val interviewCount = apps.count { it.currentStageId.contains("INTERVIEW", ignoreCase = true) }
        val offerCount = apps.count { it.currentStageId.contains("OFFER", ignoreCase = true) }

        return PipelineMetrics(
            activeCount = active.size,
            interviewRate = if (apps.isNotEmpty()) (interviewCount * 100 / apps.size) else 0,
            offerRate = if (apps.isNotEmpty()) (offerCount * 100 / apps.size) else 0
        )
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

    private fun closeApplication() {
        val current = _uiState.value as? TrackerUiState.Success ?: return
        _uiState.value = current.copy(selectedApplicationId = null)
    }

    private fun updateStage(applicationId: Long, stageId: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("tracker_stage_update"))
            repository.getApplicationById(applicationId).firstOrNull()?.let { res ->
                if (res is Result.Success) {
                    val transition = workflowEngine.transitionTo(res.data, stageId)
                    if (transition is Result.Failure) {
                        _effects.send(
                            transition.error.message ?: "Failed to update stage"
                        )
                    }
                }
            }
        }
    }

    private fun deleteApplication(id: Long) {
        viewModelScope.launch {
            repository.deleteApplication(id)
            closeApplication()
        }
    }

    private fun selectApplication(applicationId: Long) {
        val current = _uiState.value as? TrackerUiState.Success ?: return
        _uiState.value = current.copy(selectedApplicationId = applicationId)
    }

    private fun updateNotes(applicationId: Long, notes: String) {
        viewModelScope.launch {
            val result = repository.updateNotes(applicationId, notes)
            if (result is Result.Failure) {
                _effects.send(result.error.message ?: "Failed to save notes")
            }
        }
    }

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
        }
    }
}
