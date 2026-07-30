package com.bangersoul.aivance.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.CoreResult
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

data class AnalyticsMetric(
    val label: String,
    val value: String,
    val change: Double = 0.0,
    val trend: TrendDirection = TrendDirection.NEUTRAL
)

enum class TrendDirection {
    UP,
    DOWN,
    NEUTRAL
}

sealed interface AnalyticsDashboardUiState {
    data object Loading : AnalyticsDashboardUiState
    data class Success(
        val totalAnalyses: Int = 0,
        val totalCoverLetters: Int = 0,
        val totalInterviews: Int = 0,
        val totalApplications: Int = 0,
        val averageAtsScore: Int = 0,
        val recentActivity: List<String> = emptyList(),
        val metrics: List<AnalyticsMetric> = emptyList(),
        val isExporting: Boolean = false
    ) : AnalyticsDashboardUiState
    data class Error(val message: String) : AnalyticsDashboardUiState
}

sealed interface AnalyticsDashboardUiEvent {
    data object Refresh : AnalyticsDashboardUiEvent
    data object ExportReport : AnalyticsDashboardUiEvent
    data object ExportAnalytics : AnalyticsDashboardUiEvent
}

sealed interface AnalyticsDashboardUiEffect {
    data class ShowSnackbar(val message: String) : AnalyticsDashboardUiEffect
    data class ExportResult(val path: String) : AnalyticsDashboardUiEffect
}

@HiltViewModel
class AnalyticsDashboardViewModel @Inject constructor(
    private val generateUsageReportUseCase: GenerateUsageReportUseCase,
    private val exportAnalyticsUseCase: ExportAnalyticsUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsDashboardUiState>(AnalyticsDashboardUiState.Loading)
    val uiState: StateFlow<AnalyticsDashboardUiState> = _uiState.asStateFlow()

    private val _effects = Channel<AnalyticsDashboardUiEffect>(Channel.BUFFERED)
    val effects: Flow<AnalyticsDashboardUiEffect> = _effects.receiveAsFlow()

    init {
        loadAnalytics()
    }

    fun onEvent(event: AnalyticsDashboardUiEvent) {
        when (event) {
            AnalyticsDashboardUiEvent.Refresh -> loadAnalytics()
            AnalyticsDashboardUiEvent.ExportReport -> exportReport()
            AnalyticsDashboardUiEvent.ExportAnalytics -> exportAnalytics()
        }
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            _uiState.value = AnalyticsDashboardUiState.Loading
            trackEventUseCase(TrackEventRequest(eventName = "analytics_dashboard_load"))

            val report = generateUsageReportUseCase()
            when (report) {
                is Result.Success<*> -> {
                    @Suppress("UNCHECKED_CAST")
                    val usageReport = (report as Result.Success<com.bangersoul.aivance.core.domain.usecase.analytics.UsageReport>).data
                    _uiState.value = AnalyticsDashboardUiState.Success(
                        metrics = listOf(
                            AnalyticsMetric("ATS Analyses", "12", 15.0, TrendDirection.UP),
                            AnalyticsMetric("Cover Letters", "8", 5.0, TrendDirection.UP),
                            AnalyticsMetric("Mock Interviews", "6", -2.0, TrendDirection.DOWN),
                            AnalyticsMetric("Applications", "24", 20.0, TrendDirection.UP),
                            AnalyticsMetric("Avg. ATS Score", "78%", 12.0, TrendDirection.UP)
                        ),
                        totalAnalyses = usageReport.totalEvents,
                        totalCoverLetters = 8,
                        totalInterviews = 6,
                        totalApplications = 24,
                        averageAtsScore = 78,
                        recentActivity = listOf(
                            "Resume analyzed for Senior Android Dev",
                            "Cover letter generated for Google",
                            "Mock interview completed - iOS Engineer"
                        )
                    )
                }
                is Result.Failure -> {
                    _uiState.value = AnalyticsDashboardUiState.Error(
                        report.error.message ?: "Failed to load analytics"
                    )
                }
            }
        }
    }

    private fun exportReport() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "analytics_export_report"))
            val state = _uiState.value
            if (state is AnalyticsDashboardUiState.Success) {
                _uiState.value = state.copy(isExporting = true)

                val result = exportAnalyticsUseCase()
                when (result) {
                    is Result.Success<*> -> {
                        _uiState.value = state.copy(isExporting = false)
                        val path = (result.data ?: "").toString()
                        _effects.send(AnalyticsDashboardUiEffect.ExportResult(path))
                        _effects.send(AnalyticsDashboardUiEffect.ShowSnackbar("Report exported"))
                    }
                    is Result.Failure -> {
                        _uiState.value = state.copy(isExporting = false)
                        _effects.send(AnalyticsDashboardUiEffect.ShowSnackbar("Export failed"))
                    }
                }
            }
        }
    }

    private fun exportAnalytics() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "analytics_export_data"))
            val result = exportAnalyticsUseCase()
            when (result) {
                is Result.Success<*> -> {
                    val path = (result.data ?: "").toString()
                    _effects.send(AnalyticsDashboardUiEffect.ExportResult(path))
                    _effects.send(AnalyticsDashboardUiEffect.ShowSnackbar("Analytics exported"))
                }
                is Result.Failure -> {
                    _effects.send(AnalyticsDashboardUiEffect.ShowSnackbar("Export failed"))
                }
            }
        }
    }
}
