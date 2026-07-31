package com.bangersoul.aivance.core.domain.analytics

import com.bangersoul.aivance.core.common.model.Application
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KPIEngine @Inject constructor() {

    fun calculateConversionRate(applications: List<Application>, targetStageId: String): Double {
        if (applications.isEmpty()) return 0.0
        val targetCount = applications.count { it.currentStageId == targetStageId }
        return (targetCount.toDouble() / applications.size.toDouble()) * 100.0
    }

    fun calculateInterviewRate(applications: List<Application>): Double {
        // Stage IDs: SAVED, APPLIED, INTERVIEWING, OFFER, REJECTED
        val reachedInterview = applications.count {
            it.currentStageId == "INTERVIEWING" || it.currentStageId == "OFFER"
        }
        val totalApplied = applications.count { it.currentStageId != "SAVED" }
        if (totalApplied == 0) return 0.0
        return (reachedInterview.toDouble() / totalApplied.toDouble()) * 100.0
    }
}
