package com.bangersoul.aivance.core.domain.analytics

import com.bangersoul.aivance.core.common.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareerIntelligenceEngine @Inject constructor(
    private val kpiEngine: KPIEngine,
    private val scoreEngine: CareerScoreEngine
) {

    fun calculateIntelligence(
        latestAtsReports: List<AtsReport>,
        recruiters: List<Recruiter>,
        applications: List<Application>,
        interviewReadiness: Int
    ): CareerIntelligence {
        val scoreBreakdown = scoreEngine.calculateCompositeScore(
            latestAtsReports, recruiters, applications.size, interviewReadiness
        )

        val overallScore = scoreBreakdown["OVERALL"] ?: 0

        val interviewRate = kpiEngine.calculateInterviewRate(applications)

        val interviewProb = calculateInterviewProbability(latestAtsReports.firstOrNull(), recruiters.size)
        val offerProb = calculateOfferProbability(interviewReadiness, interviewRate)

        return CareerIntelligence(
            careerScore = overallScore,
            dimensionScores = scoreBreakdown,
            predictions = PredictiveMetrics(
                interviewProbability = interviewProb,
                offerProbability = offerProb,
                successExplanation = generateExplanation(overallScore, interviewProb, offerProb)
            ),
            health = listOf(
                HealthDimension("Resume", scoreBreakdown["ATS_READINESS"] ?: 0, "STABLE", "Optimize for your target role."),
                HealthDimension("Networking", scoreBreakdown["NETWORKING"] ?: 0, "UP", "Keep connecting with recruiters."),
                HealthDimension("Interview", interviewReadiness, "STABLE", "Practice more mock sessions."),
                HealthDimension("Consistency", scoreBreakdown["CONSISTENCY"] ?: 0, "STABLE", "Apply to more jobs weekly.")
            )
        )
    }

    private fun calculateInterviewProbability(latestAts: AtsReport?, networkingCount: Int): Int {
        val atsWeight = (latestAts?.overallScore ?: 0) * 0.7
        val networkingWeight = (networkingCount * 10).coerceAtMost(30)
        return (atsWeight + networkingWeight).toInt().coerceIn(0, 100)
    }

    private fun calculateOfferProbability(readiness: Int, interviewRate: Double): Int {
        return ((readiness * 0.6) + (interviewRate * 0.4)).toInt().coerceIn(0, 100)
    }

    private fun generateExplanation(overall: Int, intProb: Int, offerProb: Int): String {
        return "Based on your $overall career score, you have a $intProb% chance of landing an interview. Improving your technical readiness could boost your offer probability ($offerProb%)."
    }
}
