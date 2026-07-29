package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import com.bangersoul.aivance.feature.profile.domain.CareerRoadmap
import com.bangersoul.aivance.feature.profile.domain.RoadmapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RoadmapUiState {
    data object Idle : RoadmapUiState
    data object Loading : RoadmapUiState
    data class Success(val roadmap: CareerRoadmap) : RoadmapUiState
    data class Error(val message: String) : RoadmapUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val roadmapRepository: RoadmapRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RoadmapUiState>(RoadmapUiState.Idle)
    val uiState: StateFlow<RoadmapUiState> = _uiState.asStateFlow()

    val geminiApiKey: StateFlow<String> = userPreferencesRepository.userPreferences
        .map { it.geminiApiKey ?: "" }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    init {
        loadCurrentRoadmap()
    }

    private fun loadCurrentRoadmap() {
        viewModelScope.launch {
            roadmapRepository.getCurrentRoadmap()
                .onStart { _uiState.value = RoadmapUiState.Loading }
                .catch { _uiState.value = RoadmapUiState.Error(it.message ?: "Unknown error") }
                .collect { roadmap ->
                    _uiState.value = if (roadmap != null) {
                        RoadmapUiState.Success(roadmap)
                    } else {
                        RoadmapUiState.Idle
                    }
                }
        }
    }

    fun generateRoadmap(role: String, skills: String) {
        viewModelScope.launch {
            roadmapRepository.generateRoadmap(role, skills)
                .onStart { _uiState.value = RoadmapUiState.Loading }
                .catch { _uiState.value = RoadmapUiState.Error(it.message ?: "Failed to generate roadmap") }
                .collect { roadmap ->
                    _uiState.value = RoadmapUiState.Success(roadmap)
                }
        }
    }

    fun toggleStep(roadmapId: Long, stepId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            roadmapRepository.toggleStep(roadmapId, stepId, isCompleted)
                .catch { /* Handle error silently or show toast */ }
                .collect {
                    // The flow from getCurrentRoadmap will automatically update the UI if the repo updates the DB
                }
        }
    }

    fun updateGeminiApiKey(apiKey: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateGeminiApiKey(apiKey)
        }
    }

    fun resetRoadmap() {
        // Since getCurrentRoadmap is a flow, we can just set it to Idle locally if we had a way to clear it in repo.
        // Looking at RoadmapRepository, there's no clearRoadmap, but maybe we can just emit null from it somehow or assume a new one will replace it.
        // For now, let's just set it to Idle to show the form.
        _uiState.value = RoadmapUiState.Idle
    }

    fun calculateProgress(roadmap: CareerRoadmap): Float {
        if (roadmap.steps.isEmpty()) return 0f
        val completed = roadmap.steps.count { it.isCompleted }
        return completed.toFloat() / roadmap.steps.size
    }
}
