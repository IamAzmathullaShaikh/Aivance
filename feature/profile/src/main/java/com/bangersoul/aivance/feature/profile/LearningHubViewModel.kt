package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.career.RecommendSkillsRequest
import com.bangersoul.aivance.core.domain.usecase.career.RecommendSkillsUseCase
import com.bangersoul.aivance.core.domain.usecase.career.SuggestLearningPathRequest
import com.bangersoul.aivance.core.domain.usecase.career.SuggestLearningPathUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LearningResource(
    val title: String,
    val description: String,
    val type: ResourceType,
    val url: String = ""
)

enum class ResourceType {
    ARTICLE,
    COURSE,
    BOOK,
    VIDEO,
    TUTORIAL
}

sealed interface LearningHubUiState {
    data object Idle : LearningHubUiState
    data object Loading : LearningHubUiState
    data class Success(
        val recommendedSkills: List<String> = emptyList(),
        val suggestedResources: List<LearningResource> = emptyList()
    ) : LearningHubUiState
    data class Error(val message: String) : LearningHubUiState
}

sealed interface LearningHubUiEvent {
    data class GetRecommendations(val currentSkills: String, val targetRole: String) : LearningHubUiEvent
    data class GetLearningPath(val targetRole: String) : LearningHubUiEvent
    data object Reset : LearningHubUiEvent
}

sealed interface LearningHubUiEffect {
    data class ShowSnackbar(val message: String) : LearningHubUiEffect
    data class OpenResource(val url: String) : LearningHubUiEffect
}

@HiltViewModel
class LearningHubViewModel @Inject constructor(
    private val recommendSkillsUseCase: RecommendSkillsUseCase,
    private val suggestLearningPathUseCase: SuggestLearningPathUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LearningHubUiState>(LearningHubUiState.Idle)
    val uiState: StateFlow<LearningHubUiState> = _uiState.asStateFlow()

    private val _effects = Channel<LearningHubUiEffect>(Channel.BUFFERED)
    val effects: Flow<LearningHubUiEffect> = _effects.receiveAsFlow()

    fun onEvent(event: LearningHubUiEvent) {
        when (event) {
            is LearningHubUiEvent.GetRecommendations -> getRecommendations(event.currentSkills, event.targetRole)
            is LearningHubUiEvent.GetLearningPath -> getLearningPath(event.targetRole)
            LearningHubUiEvent.Reset -> reset()
        }
    }

    private fun getRecommendations(currentSkills: String, targetRole: String) {
        if (targetRole.isBlank()) {
            _uiState.value = LearningHubUiState.Error("Target role is required")
            return
        }
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "learning_get_recommendations"))
            _uiState.value = LearningHubUiState.Loading

            val skills = if (currentSkills.isBlank()) emptyList() else currentSkills.split(",").map { it.trim() }
            val request = RecommendSkillsRequest(
                targetRole = targetRole,
                currentSkills = skills
            )
            val result = recommendSkillsUseCase(request)
            @Suppress("UNCHECKED_CAST")
            when (result) {
                is Result.Success<*> -> {
                    val recommendations = (result as Result.Success<List<com.bangersoul.aivance.core.domain.usecase.career.SkillRecommendation>>).data
                    _uiState.value = LearningHubUiState.Success(
                        recommendedSkills = recommendations.map { it.name },
                        suggestedResources = recommendations.take(3).map { rec ->
                            LearningResource(
                                title = "Learn ${rec.name}",
                                description = rec.reason.ifBlank { "Recommended ${rec.category} skill" },
                                type = ResourceType.COURSE
                            )
                        }
                    )
                }
                is Result.Failure -> {
                    _uiState.value = LearningHubUiState.Error(result.error.message ?: "Failed")
                }
            }
        }
    }

    private fun getLearningPath(targetRole: String) {
        if (targetRole.isBlank()) return
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "learning_get_path"))
            _uiState.value = LearningHubUiState.Loading

            val request = SuggestLearningPathRequest(skillToLearn = targetRole)
            val result = suggestLearningPathUseCase(request)
            @Suppress("UNCHECKED_CAST")
            when (result) {
                is Result.Success<*> -> {
                    val path = (result as Result.Success<com.bangersoul.aivance.core.domain.usecase.career.LearningPath>).data
                    _uiState.value = LearningHubUiState.Success(
                        suggestedResources = path.resources.map { res ->
                            LearningResource(
                                title = res.name,
                                description = res.description.ifBlank { "Estimated: ${res.estimatedDuration}" },
                                type = ResourceType.COURSE
                            )
                        }
                    )
                }
                is Result.Failure -> {
                    _uiState.value = LearningHubUiState.Error(result.error.message ?: "Failed")
                }
            }
        }
    }

    private fun reset() {
        _uiState.value = LearningHubUiState.Idle
    }
}
