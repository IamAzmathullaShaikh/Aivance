package com.bangersoul.aivance.feature.ats

import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeVersion

sealed interface AtsUiEvent {
    data object Start : AtsUiEvent
    data class SelectResumeVersion(val resume: Resume, val version: ResumeVersion) : AtsUiEvent
    data class Analyze(val jobDescriptionText: String) : AtsUiEvent
    data object Reset : AtsUiEvent
}
