package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.analytics.ExportAnalyticsUseCase
import com.bangersoul.aivance.core.domain.usecase.analytics.GenerateUsageReportUseCase
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AnalyticsDashboardUiState {
    data object Loading : AnalyticsDashboardUiState
    data class Success(
        val reportSummary: String = "",
        val isExporting: Boolean = false
    ) : AnalyticsDashboardUiState
    data class Error(val message: String) : AnalyticsDashboardUiState
}

@HiltViewModel
class AnalyticsDashboardViewModel @Inject constructor(
    private val generateUsageReportUseCase: GenerateUsageReportUseCase,
    private val exportAnalyticsUseCase: ExportAnalyticsUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsDashboardUiState>(AnalyticsDashboardUiState.Loading)
    val uiState: StateFlow<AnalyticsDashboardUiState> = _uiState.asStateFlow()

    private val _effects = Channel<Unit>(Channel.BUFFERED)
    val effects: Flow<Unit> = _effects.receiveAsFlow()

    init {
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.value = AnalyticsDashboardUiState.Loading
            val report = generateUsageReportUseCase(Unit)
            if (report is Result.Success) {
                _uiState.value = AnalyticsDashboardUiState.Success(reportSummary = report.data)
            } else {
                _uiState.value = AnalyticsDashboardUiState.Error("Failed to load summary")
            }
        }
    }

    fun export() {
        viewModelScope.launch {
            exportAnalyticsUseCase(Unit)
            trackEventUseCase(TrackEventRequest("analytics_export"))
        }
    }
}
