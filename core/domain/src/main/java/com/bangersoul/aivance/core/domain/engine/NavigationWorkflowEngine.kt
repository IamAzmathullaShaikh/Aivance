package com.bangersoul.aivance.core.domain.engine

import com.bangersoul.aivance.core.common.model.CareerLifecycleStage
import com.bangersoul.aivance.core.common.model.CareerState
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maps the current Career State to navigation recommendations.
 */
@Singleton
class NavigationWorkflowEngine @Inject constructor() {

    fun getRecommendedDestination(state: CareerState): NavigationIntent {
        return when (state.lifecycleStage) {
            CareerLifecycleStage.ONBOARDING -> NavigationIntent.Action(
                label = "Setup Providers",
                route = "provider_setup"
            )
            CareerLifecycleStage.PREPARING -> NavigationIntent.Action(
                label = "Upload Resume",
                route = "resume_import"
            )
            CareerLifecycleStage.OPTIMIZING -> NavigationIntent.Action(
                label = "Fix Keywords",
                route = "ats_scanner"
            )
            CareerLifecycleStage.EXPLORING -> NavigationIntent.Action(
                label = "Search Jobs",
                route = "job_search"
            )
            CareerLifecycleStage.INTERVIEWING -> NavigationIntent.Action(
                label = "Start Practice",
                route = "prep_studio"
            )
            else -> NavigationIntent.None
        }
    }
}

sealed interface NavigationIntent {
    data class Action(val label: String, val route: String) : NavigationIntent
    data object None : NavigationIntent
}
