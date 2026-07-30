package com.bangersoul.aivance.core.domain.analytics

import com.bangersoul.aivance.core.common.dto.ConsentPreferences
import com.bangersoul.aivance.core.common.dto.HealthSummary
import com.bangersoul.aivance.core.common.dto.PerformanceMetric
import com.bangersoul.aivance.core.common.dto.ProviderMetric
import com.bangersoul.aivance.core.common.dto.SessionInfo
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Feature usage category for organizing analytics events.
 */
enum class FeatureCategory {
    AUTH,
    ONBOARDING,
    DASHBOARD,
    RESUME,
    ATS,
    COVER_LETTER,
    INTERVIEW,
    AI_CHAT,
    JOB_SEARCH,
    JOB_TRACKER,
    CAREER_ROADMAP,
    LEARNING_HUB,
    PROFILE,
    SETTINGS,
    PROVIDERS,
    NOTIFICATIONS,
    ANALYTICS
}

/**
 * An analytics event with session context and feature category.
 * Named [SessionAnalyticsEvent] to avoid conflict with [com.bangersoul.aivance.core.common.model.AnalyticsEvent].
 */
data class SessionAnalyticsEvent(
    val id: String = UUID.randomUUID().toString(),
    val eventName: String,
    val category: FeatureCategory,
    val sessionId: String? = null,
    val traceId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long? = null,
    val properties: Map<String, String> = emptyMap()
)

/**
 * Core analytics engine responsible for:
 * - Session tracking (start/end/screen views)
 * - Feature usage metrics (per-feature event counts)
 * - Event batching and upload
 * - Consent management (privacy opt-in/out)
 * - Provider metric aggregation
 * - Health summarization
 */
interface AnalyticsEngine {

    val currentSessionId: String?
    val sessionFlow: Flow<SessionInfo>
    val consentFlow: Flow<ConsentPreferences>

    // ── Session Management ─────────────────────────

    fun startSession()
    fun endSession()
    fun recordScreenView(screenName: String)

    // ── Event Tracking ─────────────────────────────

    suspend fun trackEvent(event: SessionAnalyticsEvent): CoreResult<Unit>

    suspend fun trackEvent(
        eventName: String,
        category: FeatureCategory,
        properties: Map<String, String> = emptyMap(),
        durationMs: Long? = null
    ): CoreResult<Unit>

    suspend fun trackFeatureUsage(feature: FeatureCategory): CoreResult<Unit>

    // ── Performance Metrics ────────────────────────

    fun recordMetric(metric: PerformanceMetric)

    // ── Provider Metrics ───────────────────────────

    fun recordProviderCall(providerId: String, success: Boolean, latencyMs: Long)
    fun observeProviderMetrics(): Flow<List<ProviderMetric>>

    // ── Consent Management ─────────────────────────

    fun getConsent(): ConsentPreferences
    fun updateConsent(preferences: ConsentPreferences)
    fun isTrackingAllowed(): Boolean

    // ── Aggregation & Export ───────────────────────

    suspend fun getHealthSummary(): CoreResult<HealthSummary>
    suspend fun flush()
}
