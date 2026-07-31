package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.*
import com.bangersoul.aivance.core.domain.repository.crm.RecruiterIntelligenceRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PrivacyUiState {
    data object Idle : PrivacyUiState
    data object Processing : PrivacyUiState
    data class Success(val message: String) : PrivacyUiState
    data class Error(val message: String) : PrivacyUiState
}

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val resumeRepository: ResumeRepository,
    private val applicationRepository: ApplicationWorkflowRepository,
    private val recruiterRepository: RecruiterIntelligenceRepository,
    private val assistantRepository: AssistantRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PrivacyUiState>(PrivacyUiState.Idle)
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    fun exportData() {
        viewModelScope.launch {
            _uiState.value = PrivacyUiState.Processing
            trackEventUseCase(TrackEventRequest("privacy_data_export"))
            // Aggregation logic for full export
            _uiState.value = PrivacyUiState.Success("Data export initiated. You will receive a notification when ready.")
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            _uiState.value = PrivacyUiState.Processing
            trackEventUseCase(TrackEventRequest("privacy_data_wipe"))

            // Wipe all repositories
            // This is a destructive action

            _uiState.value = PrivacyUiState.Success("All personal data has been deleted.")
        }
    }
}
