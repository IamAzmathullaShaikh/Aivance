package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.career.RecommendSkillsUseCase
import com.bangersoul.aivance.core.domain.usecase.career.SuggestLearningPathUseCase
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

            recommendSkillsUseCase(currentSkills, targetRole)
                .catch { e -> _uiState.value = LearningHubUiState.Error(e.message ?: "Failed to get recommendations") }
                .collect { result ->
                    when (result) {
                        is CoreResult.Success -> {
                            val skills = result.data.split(",").map { it.trim() }
                            _uiState.value = LearningHubUiState.Success(
                                recommendedSkills = skills,
                                suggestedResources = listOf(
                                    LearningResource(
                                        title = "Learn ${skills.firstOrNull() ?: "New Skills"}",
                                        description = "Online courses and tutorials",
                                        type = ResourceType.COURSE
                                    )
                                )
                            )
                        }
                        is CoreResult.Failure -> {
                            _uiState.value = LearningHubUiState.Error(result.error.message ?: "Failed")
                        }
                    }
                }
        }
    }

    private fun getLearningPath(targetRole: String) {
        if (targetRole.isBlank()) return
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "learning_get_path"))
            _uiState.value = LearningHubUiState.Loading

            suggestLearningPathUseCase(targetRole)
                .catch { e -> _uiState.value = LearningHubUiState.Error(e.message ?: "Failed") }
                .collect { result ->
                    when (result) {
                        is CoreResult.Success -> {
                            _uiState.value = LearningHubUiState.Success(
                                suggestedResources = listOf(
                                    LearningResource(
                                        title = result.data,
                                        description = "Suggested learning path",
                                        type = ResourceType.TUTORIAL
                                    )
                                )
                            )
                        }
                        is CoreResult.Failure -> {
                            _uiState.value = LearningHubUiState.Error(result.error.message ?: "Failed")
                        }
                    }
                }
        }
    }

    private fun reset() {
        _uiState.value = LearningHubUiState.Idle
    }
}
