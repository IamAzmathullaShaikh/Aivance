package com.bangersoul.aivance.core.domain.analytics

import com.bangersoul.aivance.core.common.model.AtsReport
import com.bangersoul.aivance.core.common.model.Recruiter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareerScoreEngine @Inject constructor() {

    fun calculateCompositeScore(
        latestAtsReports: List<AtsReport>,
        recruiters: List<Recruiter>,
        applicationCount: Int
    ): Map<String, Int> {
        val atsScore = if (latestAtsReports.isEmpty()) 0
                      else latestAtsReports.map { it.overallScore }.average().toInt()

        val networkingScore = (recruiters.size * 5).coerceAtMost(100)

        val consistencyScore = (applicationCount * 2).coerceAtMost(100)

        val overall = (atsScore + networkingScore + consistencyScore) / 3

        return mapOf(
            "OVERALL" to overall,
            "ATS_READINESS" to atsScore,
            "NETWORKING" to networkingScore,
            "CONSISTENCY" to consistencyScore
        )
    }
}
