package com.bangersoul.aivance.feature.ats

sealed interface AtsUiEffect {
    data class ShowSnackbar(val message: String) : AtsUiEffect
    data class NavigateToDetail(val resultId: Long) : AtsUiEffect
    data object UndoDeleteAvailable : AtsUiEffect
}
