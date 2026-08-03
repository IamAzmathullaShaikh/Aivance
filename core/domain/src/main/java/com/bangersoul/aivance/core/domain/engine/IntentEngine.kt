package com.bangersoul.aivance.core.domain.engine

import com.bangersoul.aivance.core.common.model.CareerLifecycleStage
import com.bangersoul.aivance.core.common.model.CareerState
import javax.inject.Inject
import javax.inject.Singleton

enum class CareerIntent {
    RESUME_HELP,
    ATS_OPTIMIZATION,
    JOB_SEARCH,
    RECRUITER_DISCOVERY,
    COVER_LETTER_GEN,
    INTERVIEW_PRACTICE,
    APPLICATION_FOLLOWUP,
    CAREER_STRATEGY,
    GENERAL_ADVICE,
    UNKNOWN
}

/**
 * Detects user intent from messages or current state to route to the correct workflow.
 */
@Singleton
class IntentEngine @Inject constructor() {

    fun detectIntent(message: String, state: CareerState): CareerIntent {
        val msg = message.lowercase()
        return when {
            msg.contains("resume") || msg.contains("cv") -> CareerIntent.RESUME_HELP
            msg.contains("ats") || msg.contains("score") || msg.contains("match") -> CareerIntent.ATS_OPTIMIZATION
            msg.contains("job") || msg.contains("search") || msg.contains("opening") -> CareerIntent.JOB_SEARCH
            msg.contains("recruiter") || msg.contains("contact") || msg.contains("hiring") -> CareerIntent.RECRUITER_DISCOVERY
            msg.contains("cover letter") || msg.contains("letter") -> CareerIntent.COVER_LETTER_GEN
            msg.contains("interview") || msg.contains("practice") || msg.contains("mock") -> CareerIntent.INTERVIEW_PRACTICE
            msg.contains("follow up") || msg.contains("applied") || msg.contains("status") -> CareerIntent.APPLICATION_FOLLOWUP
            msg.contains("goal") || msg.contains("roadmap") || msg.contains("next") -> CareerIntent.CAREER_STRATEGY
            else -> detectImplicitIntent(state)
        }
    }

    private fun detectImplicitIntent(state: CareerState): CareerIntent {
        return when (state.lifecycleStage) {
            CareerLifecycleStage.ONBOARDING -> CareerIntent.GENERAL_ADVICE
            CareerLifecycleStage.PREPARING -> CareerIntent.RESUME_HELP
            CareerLifecycleStage.OPTIMIZING -> CareerIntent.ATS_OPTIMIZATION
            CareerLifecycleStage.EXPLORING -> CareerIntent.JOB_SEARCH
            CareerLifecycleStage.APPLYING -> CareerIntent.COVER_LETTER_GEN
            CareerLifecycleStage.INTERVIEWING -> CareerIntent.INTERVIEW_PRACTICE
            CareerLifecycleStage.STRATEGIZING -> CareerIntent.CAREER_STRATEGY
        }
    }
}
