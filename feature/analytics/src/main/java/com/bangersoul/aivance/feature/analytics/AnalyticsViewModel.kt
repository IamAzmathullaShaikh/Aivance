package com.bangersoul.aivance.feature.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.AnalyticsSnapshot
import com.bangersoul.aivance.core.common.model.CareerRecommendation
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AnalyticsUiState {
    data object Loading : AnalyticsUiState
    data class Success(
        val latestSnapshot: AnalyticsSnapshot? = null,
        val recommendations: List<CareerRecommendation> = emptyList(),
        val historicalSnapshots: List<AnalyticsSnapshot> = emptyList()
    ) : AnalyticsUiState
    data class Error(val message: String) : AnalyticsUiState
}

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest("analytics_dashboard_view"))

            combine(
                analyticsRepository.getSnapshots(),
                analyticsRepository.getActiveRecommendations()
            ) { snapshotsRes, recsRes ->
                if (snapshotsRes is Result.Success && recsRes is Result.Success) {
                    val snapshots = snapshotsRes.data
                    AnalyticsUiState.Success(
                        latestSnapshot = snapshots.firstOrNull(),
                        historicalSnapshots = snapshots,
                        recommendations = recsRes.data
                    )
                } else {
                    AnalyticsUiState.Error("Failed to load analytics")
                }
            }.collect {
                _uiState.value = it
            }
        }
    }

    /**
     * Re-runs the initial data load — used by retry actions.
     */
    fun refresh() {
        _uiState.value = AnalyticsUiState.Loading
        loadData()
    }

    fun dismissRecommendation(id: Long) {
        viewModelScope.launch {
            analyticsRepository.dismissRecommendation(id)
        }
    }
}
