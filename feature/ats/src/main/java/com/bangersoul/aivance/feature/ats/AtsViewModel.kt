package com.bangersoul.aivance.feature.ats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.feature.ats.domain.AtsRepository
import com.bangersoul.aivance.feature.ats.domain.AtsResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed interface AtsUiState {
    data object Loading : AtsUiState
    data class Success(
        val latestResult: AtsResult? = null,
        val history: List<AtsResult> = emptyList(),
        val searchQuery: String = "",
        val filteredHistory: List<AtsResult> = emptyList(),
        val isSearching: Boolean = false
    ) : AtsUiState
    data object Empty : AtsUiState
    data class Error(val message: String) : AtsUiState
}

@HiltViewModel
class AtsViewModel @Inject constructor(
    private val atsRepository: AtsRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _effects = Channel<AtsUiEffect>(Channel.BUFFERED)
    val effects: Flow<AtsUiEffect> = _effects.receiveAsFlow()

    val uiState: StateFlow<AtsUiState> = atsRepository.getAtsResults()
        .map { results ->
            if (results.isEmpty()) {
                AtsUiState.Empty
            } else {
                val sorted = results.sortedByDescending { it.date }
                val query = _searchQuery.value
                val filtered = if (query.isBlank()) sorted
                else sorted.filter {
                    it.missingKeywords.any { kw -> kw.contains(query, ignoreCase = true) }
                }
                AtsUiState.Success(
                    latestResult = sorted.firstOrNull(),
                    history = sorted.drop(1),
                    searchQuery = query,
                    filteredHistory = filtered,
                    isSearching = query.isNotBlank()
                )
            }
        }
        .onStart { emit(AtsUiState.Loading) }
        .catch<AtsUiState> { emit(AtsUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AtsUiState.Loading
        )

    fun onEvent(event: AtsUiEvent) {
        when (event) {
            is AtsUiEvent.Search -> _searchQuery.value = event.query
            is AtsUiEvent.DeleteResult -> deleteResult(event.id)
            is AtsUiEvent.UndoDelete -> undoDelete(event.result)
            AtsUiEvent.Refresh -> refresh()
            AtsUiEvent.Retry -> refresh()
            is AtsUiEvent.ViewDetail -> viewDetail(event.result)
        }
    }

    private fun deleteResult(id: Long) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "ats_delete_result"))
            val result = atsRepository.getAtsResultById(id)
            if (result != null) {
                atsRepository.deleteAtsResult(result)
                sendEffect(AtsUiEffect.ShowSnackbar("Result deleted"))
            }
        }
    }

    private fun undoDelete(result: AtsResult) {
        viewModelScope.launch {
            atsRepository.saveAtsResult(result)
            sendEffect(AtsUiEffect.ShowSnackbar("Result restored"))
        }
    }

    private fun refresh() {
        viewModelScope.launch { trackEventUseCase(TrackEventRequest(eventName = "ats_refresh")) }
    }

    private fun viewDetail(result: AtsResult) {
        viewModelScope.launch { sendEffect(AtsUiEffect.NavigateToDetail(result.id)) }
    }

    private fun sendEffect(effect: AtsUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
