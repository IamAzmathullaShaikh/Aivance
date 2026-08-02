package com.bangersoul.aivance.core.domain.usecase.analytics

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.domain.analytics.AnalyticsEngine
import com.bangersoul.aivance.core.domain.analytics.FeatureCategory
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class TrackEventRequest(
    val eventName: String,
    val properties: Map<String, String> = emptyMap(),

    /**
     * Optional explicit feature category. When null the category is inferred
     * from the event-name prefix ([inferCategory]).
     */
    val category: FeatureCategory? = null
)

/**
 * Records a career-related event.
 *
 * Phase 4 (STEP 6): the previous implementation validated the request but
 * dropped the event. Events are now persisted through [AnalyticsEngine], which
 * batches them into Room via `AiAnalyticsDao` (respecting analytics consent).
 * The Dashboard "Recent Activity" feed and the Analytics dashboard are driven
 * by the same persisted data.
 */
class TrackEventUseCase @Inject constructor(
    private val analyticsEngine: AnalyticsEngine
) : UseCase<TrackEventRequest, CoreResult<Unit>>() {

    override suspend operator fun invoke(input: TrackEventRequest): CoreResult<Unit> {
        if (input.eventName.isBlank()) {
            return Result.Failure(ValidationError("eventName", "Event name cannot be blank."))
        }

        return analyticsEngine.trackEvent(
            eventName = input.eventName,
            category = input.category ?: inferCategory(input.eventName),
            properties = input.properties
        )
    }

    private fun inferCategory(eventName: String): FeatureCategory = when {
        eventName.startsWith("resume") -> FeatureCategory.RESUME
        eventName.startsWith("cover_letter") -> FeatureCategory.COVER_LETTER
        eventName.startsWith("ats") -> FeatureCategory.ATS
        eventName.startsWith("auth") -> FeatureCategory.AUTH
        eventName.startsWith("interview") -> FeatureCategory.INTERVIEW
        eventName.startsWith("ai_chat") || eventName.startsWith("chat") -> FeatureCategory.AI_CHAT
        eventName.startsWith("job") -> FeatureCategory.JOB_SEARCH
        eventName.startsWith("pipeline") || eventName.startsWith("tracker") ||
            eventName.startsWith("application") -> FeatureCategory.JOB_TRACKER
        eventName.startsWith("analytics") -> FeatureCategory.ANALYTICS
        eventName.startsWith("settings") -> FeatureCategory.SETTINGS
        eventName.startsWith("provider") -> FeatureCategory.PROVIDERS
        eventName.startsWith("profile") -> FeatureCategory.PROFILE
        eventName.startsWith("onboarding") -> FeatureCategory.ONBOARDING
        eventName.startsWith("dashboard") -> FeatureCategory.DASHBOARD
        eventName.startsWith("notification") -> FeatureCategory.NOTIFICATIONS
        else -> FeatureCategory.GENERAL
    }
}
