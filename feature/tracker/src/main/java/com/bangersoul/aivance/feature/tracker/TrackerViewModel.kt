package com.bangersoul.aivance.feature.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.Application
import com.bangersoul.aivance.core.common.model.ApplicationStage
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.ApplicationWorkflowRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TrackerUiState {
    data object Loading : TrackerUiState
    data class Success(
        val applications: List<Application> = emptyList(),
        val stages: List<ApplicationStage> = emptyList()
    ) : TrackerUiState
    data class Error(val message: String) : TrackerUiState
}

sealed interface TrackerUiEvent {
    data class UpdateStage(val applicationId: Long, val stageId: String) : TrackerUiEvent
    data class DeleteApplication(val id: Long) : TrackerUiEvent
    data object Refresh : TrackerUiEvent
}

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val repository: ApplicationWorkflowRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrackerUiState>(TrackerUiState.Loading)
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private val _effects = Channel<Unit>(Channel.BUFFERED)
    val effects: Flow<Unit> = _effects.receiveAsFlow()

    init {
        loadData()
    }

    fun onEvent(event: TrackerUiEvent) {
        when (event) {
            is TrackerUiEvent.UpdateStage -> updateStage(event.applicationId, event.stageId)
            is TrackerUiEvent.DeleteApplication -> deleteApplication(event.id)
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
            }.collect {
                _uiState.value = it
            }
        }
    }

    private fun updateStage(applicationId: Long, stageId: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("tracker_stage_update"))
            // We'd ideally use WorkflowEngine here
            repository.getApplicationById(applicationId).firstOrNull()?.let { res ->
                if (res is Result.Success) {
                    repository.saveApplication(res.data.copy(currentStageId = stageId))
                }
            }
        }
    }

    private fun deleteApplication(id: Long) {
        viewModelScope.launch {
            repository.deleteApplication(id)
            loadData()
        }
    }

    // Legacy support for older screens if needed
    fun addApplication(company: String, role: String, status: Any) {}
    fun updateStatus(id: Long, status: Any) {}
}
