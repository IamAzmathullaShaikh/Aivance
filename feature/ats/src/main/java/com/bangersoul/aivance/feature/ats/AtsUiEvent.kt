package com.bangersoul.aivance.feature.ats

import com.bangersoul.aivance.feature.ats.domain.AtsResult

sealed interface AtsUiEvent {
    data class Search(val query: String) : AtsUiEvent
    data class DeleteResult(val id: Long) : AtsUiEvent
    data class UndoDelete(val result: AtsResult) : AtsUiEvent
    data object Refresh : AtsUiEvent
    data object Retry : AtsUiEvent
    data class ViewDetail(val result: AtsResult) : AtsUiEvent
}
