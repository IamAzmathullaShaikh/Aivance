package com.bangersoul.aivance.feature.ats

import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeVersion

sealed interface AtsUiEvent {
    data object Start : AtsUiEvent
    data class SelectResumeVersion(val resume: Resume, val version: ResumeVersion) : AtsUiEvent

    /** Live keystroke event — feeds the debounced reactive analysis flow. */
    data class UpdateJobDescription(val jobDescriptionText: String) : AtsUiEvent

    /** Explicit "Start Match Analysis" trigger (routes through the live flow). */
    data class Analyze(val jobDescriptionText: String) : AtsUiEvent

    data object Reset : AtsUiEvent
    data object GenerateCoverLetter : AtsUiEvent
    data object ExportReport : AtsUiEvent
}
