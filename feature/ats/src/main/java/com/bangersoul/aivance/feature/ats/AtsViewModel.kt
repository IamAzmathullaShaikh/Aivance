package com.bangersoul.aivance.feature.ats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.feature.ats.domain.AtsRepository
import com.bangersoul.aivance.feature.ats.domain.AtsResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface AtsUiState {
    data object Loading : AtsUiState
    data class Success(
        val latestResult: AtsResult?,
        val history: List<AtsResult>
    ) : AtsUiState
    data class Error(val message: String) : AtsUiState
}

@HiltViewModel
class AtsViewModel @Inject constructor(
    private val atsRepository: AtsRepository
) : ViewModel() {

    val uiState: StateFlow<AtsUiState> = atsRepository.getAtsResults()
        .map { results ->
            if (results.isEmpty()) {
                AtsUiState.Success(null, emptyList())
            } else {
                val sorted = results.sortedByDescending { it.date }
                AtsUiState.Success(
                    latestResult = sorted.first(),
                    history = sorted.drop(1)
                )
            }
        }
        .catch<AtsUiState> { emit(AtsUiState.Error(it.message ?: "Unknown error occurred")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AtsUiState.Loading
        )
}
