package com.bangersoul.aivance.core.data.analytics

import com.bangersoul.aivance.core.common.dto.ConsentPreferences
import com.bangersoul.aivance.core.common.dto.HealthSummary
import com.bangersoul.aivance.core.common.dto.PerformanceMetric
import com.bangersoul.aivance.core.common.dto.ProviderMetric
import com.bangersoul.aivance.core.common.dto.SessionInfo
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.database.dao.AiAnalyticsDao
import com.bangersoul.aivance.core.database.model.AnalyticsEventEntity
import com.bangersoul.aivance.core.domain.analytics.AnalyticsEngine
import com.bangersoul.aivance.core.domain.analytics.FeatureCategory
import com.bangersoul.aivance.core.domain.analytics.SessionAnalyticsEvent
import com.bangersoul.aivance.core.domain.telemetry.TelemetryEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [AnalyticsEngine].
 *
 * Features:
 * - Session tracking with start/end/screen views
 * - Event batching with batch persistence to Room DB
 * - Privacy-aware consent management
 * - Provider metric aggregation
 * - Feature usage tracking via atomic counters
 * - Health summary generation
 */
@Singleton
class AnalyticsEngineImpl @Inject constructor(
    private val analyticsDao: AiAnalyticsDao,
    private val telemetryEngine: TelemetryEngine
) : AnalyticsEngine {

    companion object {
        private const val BATCH_SIZE = 50
    }

    // ── Session State ────────────────────────────────
    private var _sessionInfo: SessionInfo? = null
    private val _sessionFlow = MutableStateFlow<SessionInfo?>(null)
    override val sessionFlow: Flow<SessionInfo>
        get() = _sessionFlow.asStateFlow().map { it ?: SessionInfo("", 0) }

    override val currentSessionId: String?
        get() = _sessionInfo?.sessionId

    // ── Consent State ────────────────────────────────
    private val _consent = MutableStateFlow(ConsentPreferences())
    override val consentFlow: Flow<ConsentPreferences> = _consent.asStateFlow()

    // ── Feature Usage Counters ───────────────────────
    private val featureCounters = ConcurrentHashMap<FeatureCategory, AtomicInteger>()

    // ── Provider Metrics ─────────────────────────────
    private val _providerMetrics = MutableStateFlow<Map<String, ProviderMetric>>(emptyMap())
    private val providerMetricsMap = ConcurrentHashMap<String, ProviderMetrics>()

    private data class ProviderMetrics(
        var totalCalls: Int = 0,
        var successCount: Int = 0,
        var failureCount: Int = 0,
        var totalLatencyMs: Long = 0L,
        val latencies: MutableList<Long> = mutableListOf(),
        var lastCallTime: Long? = null
    )

    // ── Event Buffer ────────────────────────────────
    private val eventBuffer = mutableListOf<AnalyticsEventEntity>()
    private val bufferLock = java.util.concurrent.locks.ReentrantLock()

    // ── Session Management ──────────────────────────

    override fun startSession() {
        val sessionId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        _sessionInfo = SessionInfo(sessionId = sessionId, startTime = now)
        _sessionFlow.value = _sessionInfo
        Timber.d("Session started: %s", sessionId)
        telemetryEngine.info("Analytics", "Session started", mapOf("sessionId" to sessionId))
    }

    override fun endSession() {
        val session = _sessionInfo ?: return
        val now = System.currentTimeMillis()
        val updatedSession = session.copy(endTime = now, durationMs = now - session.startTime)
        _sessionInfo = null
        _sessionFlow.value = updatedSession
        Timber.d("Session ended: %s (duration: %dms)", session.sessionId, updatedSession.durationMs)
    }

    override fun recordScreenView(screenName: String) {
        _sessionInfo?.let { session ->
            _sessionInfo = session.copy(screenViews = session.screenViews + screenName)
        }
    }

    // ── Event Tracking ─────────────────────────────

    override suspend fun trackEvent(event: SessionAnalyticsEvent): CoreResult<Unit> {
        if (!isTrackingAllowed()) return Result.Success(Unit)
        return runCatchingCore {
            persistEvent(event)
        }
    }

    override suspend fun trackEvent(
        eventName: String,
        category: FeatureCategory,
        properties: Map<String, String>,
        durationMs: Long?
    ): CoreResult<Unit> {
        return trackEvent(
            SessionAnalyticsEvent(
                id = UUID.randomUUID().toString(),
                eventName = eventName,
                category = category,
                sessionId = currentSessionId,
                properties = properties,
                durationMs = durationMs
            )
        )
    }

    override suspend fun trackFeatureUsage(feature: FeatureCategory): CoreResult<Unit> {
        featureCounters.getOrPut(feature) { AtomicInteger(0) }.incrementAndGet()
        return trackEvent(
            eventName = "feature_used",
            category = feature,
            properties = mapOf("feature" to feature.name)
        )
    }

    // ── Performance Metrics ─────────────────────────

    override fun recordMetric(metric: PerformanceMetric) {
        if (!_consent.value.performanceMetricsEnabled) return
        telemetryEngine.recordMetric(metric)
    }

    // ── Provider Metrics ───────────────────────────

    override fun recordProviderCall(providerId: String, success: Boolean, latencyMs: Long) {
        val metrics = providerMetricsMap.getOrPut(providerId) { ProviderMetrics() }

        metrics.totalCalls++
        if (success) metrics.successCount++ else metrics.failureCount++
        metrics.totalLatencyMs += latencyMs
        metrics.latencies.add(latencyMs)
        metrics.lastCallTime = System.currentTimeMillis()

        if (metrics.latencies.size > 100) {
            val trimmed = metrics.latencies.takeLast(100)
            metrics.latencies.clear()
            metrics.latencies.addAll(trimmed)
        }

        updateProviderMetricState(providerId, metrics)
    }

    override fun observeProviderMetrics(): Flow<List<ProviderMetric>> {
        return _providerMetrics.asStateFlow().map { it.values.toList() }
    }

    // ── Consent Management ─────────────────────────

    override fun getConsent(): ConsentPreferences = _consent.value

    override fun updateConsent(preferences: ConsentPreferences) {
        _consent.value = preferences
        Timber.d("Consent updated: analytics=%s, crash=%s, perf=%s",
            preferences.analyticsEnabled, preferences.crashReportingEnabled,
            preferences.performanceMetricsEnabled)
    }

    override fun isTrackingAllowed(): Boolean = _consent.value.analyticsEnabled

    // ── Aggregation & Export ───────────────────────

    override suspend fun getHealthSummary(): CoreResult<HealthSummary> {
        val providerMetrics = _providerMetrics.value
        return Result.Success(
            HealthSummary(
                providersHealthy = providerMetrics.values.count { it.isHealthy },
                providersTotal = providerMetrics.size,
                lastSyncTime = System.currentTimeMillis(),
                sessionCount = if (_sessionInfo != null) 1 else 0,
                crashFreeRate = 100.0
            )
        )
    }

    override suspend fun flush() {
        bufferLock.lock()
        val hasPending = eventBuffer.isNotEmpty()
        bufferLock.unlock()

        if (hasPending) {
            flushBuffer()
        }
    }

    // ── Private Helpers ────────────────────────────

    private suspend fun persistEvent(event: SessionAnalyticsEvent) {
        val entity = AnalyticsEventEntity(
            eventName = event.eventName,
            params = event.properties + mapOf(
                "category" to event.category.name,
                "sessionId" to (event.sessionId ?: currentSessionId ?: "unknown")
            ),
            timestamp = Instant.now()
        )

        bufferLock.lock()
        eventBuffer.add(entity)
        val shouldFlush = eventBuffer.size >= BATCH_SIZE
        bufferLock.unlock()

        if (shouldFlush) {
            flushBuffer()
        }
    }

    private suspend fun flushBuffer() {
        bufferLock.lock()
        val batch = eventBuffer.toList()
        eventBuffer.clear()
        bufferLock.unlock()

        try {
            if (batch.isNotEmpty()) {
                analyticsDao.insertAnalyticsEvents(batch)
                Timber.d("Flushed %d analytics events to database", batch.size)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to flush analytics events")
        }
    }

    private fun updateProviderMetricState(providerId: String, metrics: ProviderMetrics) {
        val avgLatency = if (metrics.totalCalls > 0)
            metrics.totalLatencyMs.toDouble() / metrics.totalCalls else 0.0

        val sortedLatencies = metrics.latencies.sorted()
        val p95Index = (sortedLatencies.size * 0.95).toInt().coerceAtMost(sortedLatencies.size - 1)
        val p95Latency = if (sortedLatencies.isNotEmpty() && p95Index >= 0)
            sortedLatencies[p95Index].toDouble() else 0.0

        val providerMetric = ProviderMetric(
            providerId = providerId,
            totalCalls = metrics.totalCalls,
            successCount = metrics.successCount,
            failureCount = metrics.failureCount,
            avgLatencyMs = avgLatency,
            p95LatencyMs = p95Latency,
            lastCallTime = metrics.lastCallTime,
            isHealthy = metrics.totalCalls == 0 ||
                    metrics.failureCount.toDouble() / metrics.totalCalls < 0.3
        )

        val current = _providerMetrics.value.toMutableMap()
        current[providerId] = providerMetric
        _providerMetrics.tryEmit(current)
    }
}
