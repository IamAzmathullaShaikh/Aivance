package com.bangersoul.aivance.core.domain.analytics

import com.bangersoul.aivance.core.common.model.CareerIntelligence
import com.bangersoul.aivance.core.common.model.PredictiveMetrics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareerForecastEngine @Inject constructor() {

    fun simulate(
        current: CareerIntelligence,
        hypotheticalAts: Int? = null,
        hypotheticalReadiness: Int? = null
    ): CareerIntelligence {
        val newAts = hypotheticalAts ?: current.dimensionScores["ATS_READINESS"] ?: 0
        val newReadiness = hypotheticalReadiness ?: current.dimensionScores["INTERVIEW_READINESS"] ?: 0

        // Simple linear simulation
        val intProb = ((newAts * 0.7) + 20).toInt().coerceIn(0, 100)
        val offerProb = ((newReadiness * 0.8) + 10).toInt().coerceIn(0, 100)

        return current.copy(
            careerScore = (current.careerScore + (newAts - (current.dimensionScores["ATS_READINESS"] ?: 0)) / 2).coerceIn(0, 100),
            predictions = current.predictions.copy(
                interviewProbability = intProb,
                offerProbability = offerProb,
                successExplanation = "Simulation shows that improving your ATS score to $newAts would increase interview probability by ${intProb - current.predictions.interviewProbability}%."
            )
        )
    }
}
