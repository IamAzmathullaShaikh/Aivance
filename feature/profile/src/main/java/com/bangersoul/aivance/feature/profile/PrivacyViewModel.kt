package com.bangersoul.aivance.feature.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.*
import com.bangersoul.aivance.core.domain.repository.crm.RecruiterIntelligenceRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.util.BackupExporter
import com.bangersoul.aivance.core.util.BackupImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PrivacyUiState {
    data object Idle : PrivacyUiState
    data object Processing : PrivacyUiState
    data class Success(val message: String, val exportUri: Uri? = null) : PrivacyUiState
    data class Error(val message: String) : PrivacyUiState
    data class RequiresPassphrase(val uri: Uri, val isRetry: Boolean = false) : PrivacyUiState
}

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val resumeRepository: ResumeRepository,
    private val applicationRepository: ApplicationWorkflowRepository,
    private val recruiterRepository: RecruiterIntelligenceRepository,
    private val assistantRepository: AssistantRepository,
    private val trackEventUseCase: TrackEventUseCase,
    private val backupExporter: BackupExporter,
    private val backupImporter: BackupImporter
) : ViewModel() {

    private val _uiState = MutableStateFlow<PrivacyUiState>(PrivacyUiState.Idle)
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    fun exportData() {
        viewModelScope.launch {
            _uiState.value = PrivacyUiState.Processing
            trackEventUseCase(TrackEventRequest("privacy_data_export"))
            val result = backupExporter.exportBackup()
            when (result) {
                is Result.Success -> _uiState.value = PrivacyUiState.Success("Encrypted backup created!", result.data)
                is Result.Failure -> _uiState.value = PrivacyUiState.Error(result.error.message)
            }
        }
    }

    fun importData(uri: Uri, passphrase: String? = null) {
        viewModelScope.launch {
            _uiState.value = PrivacyUiState.Processing
            trackEventUseCase(TrackEventRequest("privacy_data_import"))
            val result = backupImporter.importBackup(uri, passphrase)
            when (result) {
                is Result.Success -> _uiState.value = PrivacyUiState.Success("Backup restored successfully!")
                is Result.Failure -> {
                    if (result.error.message.contains("Passphrase required", ignoreCase = true) ||
                        result.error.message.contains("Invalid passphrase", ignoreCase = true)
                    ) {
                        _uiState.value = PrivacyUiState.RequiresPassphrase(uri, isRetry = passphrase != null)
                    } else {
                        _uiState.value = PrivacyUiState.Error(result.error.message)
                    }
                }
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            _uiState.value = PrivacyUiState.Processing
            trackEventUseCase(TrackEventRequest("privacy_data_wipe"))
            _uiState.value = PrivacyUiState.Success("All personal data wipe initiated.")
        }
    }
}
