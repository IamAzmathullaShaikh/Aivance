package com.bangersoul.aivance.feature.ats

sealed interface AtsUiEffect {
    data class ShowSnackbar(val message: String) : AtsUiEffect
    data class NavigateToCoverLetter(val reportId: Long) : AtsUiEffect
}
