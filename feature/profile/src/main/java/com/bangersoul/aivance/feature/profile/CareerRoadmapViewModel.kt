package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.career.GenerateCareerRoadmapRequest
import com.bangersoul.aivance.core.domain.usecase.career.GenerateCareerRoadmapUseCase
import com.bangersoul.aivance.core.domain.usecase.career.RecommendSkillsUseCase
import com.bangersoul.aivance.core.domain.usecase.career.SuggestLearningPathUseCase
import com.bangersoul.aivance.feature.profile.domain.CareerRoadmap
import com.bangersoul.aivance.feature.profile.domain.RoadmapRepository
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

sealed interface CareerRoadmapUiState {
    data object Idle : CareerRoadmapUiState
    data object Loading : CareerRoadmapUiState
    data class Success(
        val roadmap: CareerRoadmap,
        val progressPercent: Float = 0f
    ) : CareerRoadmapUiState
    data class Error(val message: String) : CareerRoadmapUiState
}

sealed interface CareerRoadmapUiEvent {
    data class Generate(val targetRole: String, val currentSkills: String) : CareerRoadmapUiEvent
    data class ToggleStep(val roadmapId: Long, val stepId: Long, val isCompleted: Boolean) : CareerRoadmapUiEvent
    data object Refresh : CareerRoadmapUiEvent
}

sealed interface CareerRoadmapUiEffect {
    data class ShowSnackbar(val message: String) : CareerRoadmapUiEffect
}

@HiltViewModel
class CareerRoadmapViewModel @Inject constructor(
    private val roadmapRepository: RoadmapRepository,
    private val generateCareerRoadmapUseCase: GenerateCareerRoadmapUseCase,
    private val recommendSkillsUseCase: RecommendSkillsUseCase,
    private val suggestLearningPathUseCase: SuggestLearningPathUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CareerRoadmapUiState>(CareerRoadmapUiState.Idle)
    val uiState: StateFlow<CareerRoadmapUiState> = _uiState.asStateFlow()

    private val _effects = Channel<CareerRoadmapUiEffect>(Channel.BUFFERED)
    val effects: Flow<CareerRoadmapUiEffect> = _effects.receiveAsFlow()

    init {
        loadCurrentRoadmap()
    }

    fun onEvent(event: CareerRoadmapUiEvent) {
        when (event) {
            is CareerRoadmapUiEvent.Generate -> generate(event.targetRole, event.currentSkills)
            is CareerRoadmapUiEvent.ToggleStep -> toggleStep(event.roadmapId, event.stepId, event.isCompleted)
            CareerRoadmapUiEvent.Refresh -> loadCurrentRoadmap()
        }
    }

    private fun loadCurrentRoadmap() {
        viewModelScope.launch {
            roadmapRepository.getCurrentRoadmap()
                .catch { e -> _uiState.value = CareerRoadmapUiState.Error(e.message ?: "Failed to load roadmap") }
                .collect { roadmap ->
                    _uiState.value = if (roadmap != null) {
                        val progress = calculateProgress(roadmap)
                        CareerRoadmapUiState.Success(roadmap = roadmap, progressPercent = progress)
                    } else {
                        CareerRoadmapUiState.Idle
                    }
                }
        }
    }

    private fun generate(targetRole: String, currentSkills: String) {
        if (targetRole.isBlank()) {
            _uiState.value = CareerRoadmapUiState.Error("Target role is required")
            return
        }
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "roadmap_generate"))
            _uiState.value = CareerRoadmapUiState.Loading

            val skills = if (currentSkills.isBlank()) emptyList() else currentSkills.split(",").map { it.trim() }
            val request = GenerateCareerRoadmapRequest(
                targetRole = targetRole,
                currentSkills = skills
            )
            val result = generateCareerRoadmapUseCase(request)
            when (result) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val roadmap = (result as Result.Success<com.bangersoul.aivance.core.common.model.CareerRoadmap>).data
                    val progress = calculateFromCoreRoadmap(roadmap)
                    _uiState.value = CareerRoadmapUiState.Success(
                        roadmap = mapToFeatureRoadmap(roadmap),
                        progressPercent = progress
                    )
                    _effects.send(CareerRoadmapUiEffect.ShowSnackbar("Career roadmap generated"))
                }
                is Result.Failure -> {
                    _uiState.value = CareerRoadmapUiState.Error(
                        result.error.message ?: "Failed to generate roadmap"
                    )
                }
            }
        }
    }

    private fun toggleStep(roadmapId: Long, stepId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            roadmapRepository.toggleStep(roadmapId, stepId, isCompleted)
                .catch { e -> _effects.send(CareerRoadmapUiEffect.ShowSnackbar(e.message ?: "Failed to update")) }
                .collect { }
        }
    }

    fun calculateProgress(roadmap: CareerRoadmap): Float {
        if (roadmap.steps.isEmpty()) return 0f
        val completed = roadmap.steps.count { it.isCompleted }
        return completed.toFloat() / roadmap.steps.size
    }

    private fun calculateFromCoreRoadmap(roadmap: com.bangersoul.aivance.core.common.model.CareerRoadmap): Float {
        if (roadmap.steps.isEmpty()) return 0f
        val completed = roadmap.steps.count { it.isCompleted }
        return completed.toFloat() / roadmap.steps.size
    }

    private fun mapToFeatureRoadmap(roadmap: com.bangersoul.aivance.core.common.model.CareerRoadmap): CareerRoadmap {
        return CareerRoadmap(
            id = 0L,
            targetRole = roadmap.targetRole,
            currentSkills = "",
            steps = roadmap.steps.mapIndexed { index, step ->
                com.bangersoul.aivance.feature.profile.domain.RoadmapStep(
                    id = index.toLong(),
                    title = step.title,
                    description = step.description ?: "",
                    order = step.stepOrder,
                    isCompleted = step.isCompleted
                )
            }
        )
    }
}
