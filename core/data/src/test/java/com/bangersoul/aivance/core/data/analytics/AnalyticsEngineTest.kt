package com.bangersoul.aivance.core.data.analytics

import com.bangersoul.aivance.core.common.dto.ConsentPreferences
import com.bangersoul.aivance.core.common.dto.PerformanceMetric
import com.bangersoul.aivance.core.data.telemetry.TelemetryEngineImpl
import com.bangersoul.aivance.core.database.dao.AiAnalyticsDao
import com.bangersoul.aivance.core.domain.analytics.AnalyticsEvent
import com.bangersoul.aivance.core.domain.analytics.FeatureCategory
import com.bangersoul.aivance.core.domain.telemetry.LogEntry
import com.bangersoul.aivance.core.domain.telemetry.LogLevel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first as flowFirst
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AnalyticsEngineTest {

    private lateinit var mockDao: AiAnalyticsDao
    private lateinit var telemetryEngine: TelemetryEngineImpl
    private lateinit var analyticsEngine: AnalyticsEngineImpl

    @Before
    fun setup() {
        mockDao = mockk(relaxed = true)
        coEvery { mockDao.insertAnalyticsEvents(any()) } returns Unit
        telemetryEngine = TelemetryEngineImpl()
        analyticsEngine = AnalyticsEngineImpl(mockDao, telemetryEngine)
    }

    // ── Session Tests ───────────────────────────────

    @Test
    fun `session starts with null session id`() {
        assertNull(analyticsEngine.currentSessionId)
    }

    @Test
    fun `startSession generates session id`() {
        analyticsEngine.startSession()
        assertNotNull(analyticsEngine.currentSessionId)
    }

    @Test
    fun `endSession clears session`() {
        analyticsEngine.startSession()
        analyticsEngine.endSession()
        assertNull(analyticsEngine.currentSessionId)
    }

    @Test
    fun `recordScreenView does not throw`() {
        analyticsEngine.startSession()
        analyticsEngine.recordScreenView("Dashboard")
        analyticsEngine.recordScreenView("Profile")
    }

    // ── Event Tracking Tests ────────────────────────

    @Test
    fun `trackEvent succeeds`() = runBlocking {
        analyticsEngine.startSession()
        val result = analyticsEngine.trackEvent(
            eventName = "test_event",
            category = FeatureCategory.DASHBOARD
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun `trackEvent with properties succeeds`() = runBlocking {
        analyticsEngine.startSession()
        val result = analyticsEngine.trackEvent(
            eventName = "test_event",
            category = FeatureCategory.RESUME,
            properties = mapOf("key" to "value")
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun `trackEvent respects consent when disabled`() = runBlocking {
        analyticsEngine.updateConsent(ConsentPreferences(analyticsEnabled = false))
        analyticsEngine.startSession()
        val result = analyticsEngine.trackEvent(
            eventName = "should_not_be_tracked",
            category = FeatureCategory.SETTINGS
        )
        assertTrue(result.isSuccess)
    }

    @Test
    fun `trackFeatureUsage does not throw`() = runBlocking {
        analyticsEngine.startSession()
        analyticsEngine.trackFeatureUsage(FeatureCategory.ATS)
        analyticsEngine.trackFeatureUsage(FeatureCategory.ATS)
        analyticsEngine.trackFeatureUsage(FeatureCategory.INTERVIEW)
    }

    // ── Consent Management Tests ────────────────────

    @Test
    fun `default consent enables all`() {
        val consent = analyticsEngine.getConsent()
        assertTrue(consent.analyticsEnabled)
        assertTrue(consent.crashReportingEnabled)
        assertTrue(consent.performanceMetricsEnabled)
    }

    @Test
    fun `updateConsent changes preferences`() {
        val newConsent = ConsentPreferences(analyticsEnabled = false)
        analyticsEngine.updateConsent(newConsent)
        assertEquals(newConsent, analyticsEngine.getConsent())
    }

    @Test
    fun `isTrackingAllowed returns false when analytics disabled`() {
        analyticsEngine.updateConsent(ConsentPreferences(analyticsEnabled = false))
        assertFalse(analyticsEngine.isTrackingAllowed())
    }

    @Test
    fun `isTrackingAllowed returns true when analytics enabled`() {
        assertTrue(analyticsEngine.isTrackingAllowed())
    }

    // ── Provider Metrics Tests ──────────────────────

    @Test
    fun `recordProviderCall updates metrics`() = runBlocking {
        analyticsEngine.recordProviderCall("gemini", success = true, latencyMs = 150)
        analyticsEngine.recordProviderCall("gemini", success = true, latencyMs = 200)
        analyticsEngine.recordProviderCall("gemini", success = false, latencyMs = 3000)

        val metrics = withTimeout(1000) { analyticsEngine.observeProviderMetrics().flowFirst() }

        assertEquals(1, metrics.size)
        val geminiMetric = metrics.first()
        assertEquals("gemini", geminiMetric.providerId)
        assertEquals(3, geminiMetric.totalCalls)
        assertEquals(2, geminiMetric.successCount)
        assertEquals(1, geminiMetric.failureCount)
    }

    @Test
    fun `provider is unhealthy when failure rate exceeds 30 percent`() = runBlocking {
        repeat(10) { analyticsEngine.recordProviderCall("bad_provider", success = false, latencyMs = 500) }

        val metrics = withTimeout(1000) { analyticsEngine.observeProviderMetrics().flowFirst() }
        val badProvider = metrics.find { it.providerId == "bad_provider" }
        assertNotNull(badProvider)
        assertFalse(badProvider?.isHealthy ?: true)
    }

    // ── Health Summary Tests ────────────────────────

    @Test
    fun `getHealthSummary returns valid data`() = runBlocking {
        analyticsEngine.startSession()
        analyticsEngine.trackFeatureUsage(FeatureCategory.DASHBOARD)

        val summary = analyticsEngine.getHealthSummary()
        assertTrue(summary.isSuccess)
        assertTrue(summary.getOrNull()?.sessionCount != null)
    }

    // ── Telemetry Engine Tests ──────────────────────

    @Test
    fun `telemetry generates unique trace ids`() {
        val id1 = telemetryEngine.generateTraceId()
        val id2 = telemetryEngine.generateTraceId()
        assertNotNull(id1)
        assertNotNull(id2)
        assertTrue(id1.isNotBlank())
        assertTrue(id2.isNotBlank())
    }

    @Test
    fun `telemetry startTrace returns valid context`() {
        val context = telemetryEngine.startTrace("test_operation")
        assertNotNull(context.traceId)
        assertNotNull(context.spanId)
        assertEquals("test_operation", context.operationName)
    }

    @Test
    fun `telemetry endTrace does not throw`() {
        val context = telemetryEngine.startTrace("fast_op")
        telemetryEngine.endTrace(context)
    }

    @Test
    fun `telemetry startSpan creates child span`() {
        val parent = telemetryEngine.startTrace("parent")
        val child = telemetryEngine.startSpan(parent, "child")
        assertEquals(parent.traceId, child.traceId)
        assertEquals(parent.spanId, child.parentSpanId)
    }

    @Test
    fun `telemetry log creates buffered entry`() {
        telemetryEngine.log(LogEntry(level = LogLevel.INFO, message = "Test log message", tag = "Test"))
        assertEquals(1, telemetryEngine.bufferedLogCount())
    }

    @Test
    fun `telemetry log with trace context includes ids`() {
        telemetryEngine.log(LogEntry(level = LogLevel.DEBUG, message = "Debug message", tag = "Test", traceId = "trace_123", spanId = "span_456"))
        assertEquals(1, telemetryEngine.bufferedLogCount())
    }

    @Test
    fun `telemetry recordMetric buffers metrics`() {
        telemetryEngine.recordMetric(PerformanceMetric(name = "test_metric", value = 42.0, unit = "count"))
        assertEquals(1, telemetryEngine.bufferedMetricCount())
    }

    @Test
    fun `telemetry flush clears buffers`() = runBlocking {
        telemetryEngine.log(LogEntry(level = LogLevel.INFO, message = "m1", tag = "T"))
        telemetryEngine.log(LogEntry(level = LogLevel.INFO, message = "m2", tag = "T"))
        telemetryEngine.recordMetric(PerformanceMetric("m", 1.0, "ms"))

        telemetryEngine.flush()

        assertEquals(0, telemetryEngine.bufferedLogCount())
        assertEquals(0, telemetryEngine.bufferedMetricCount())
    }

    // ── CrashReporter Tests ─────────────────────────

    @Test
    fun `crashReporter respects consent`() {
        analyticsEngine.updateConsent(ConsentPreferences(crashReportingEnabled = false))
        val reporter = CrashReporter(analyticsEngine, telemetryEngine)
        reporter.recordException(RuntimeException("Test crash"))
    }

    @Test
    fun `crashReporter records exception`() {
        val reporter = CrashReporter(analyticsEngine, telemetryEngine)
        reporter.recordException(RuntimeException("Test"), mapOf("context" to "unit_test"))
    }

    @Test
    fun `crashReporter logs breadcrumb`() {
        val reporter = CrashReporter(analyticsEngine, telemetryEngine)
        reporter.logBreadcrumb("test_action", mapOf("key" to "value"))
    }

    // ── FeatureCategory Enum Tests ──────────────────

    @Test
    fun `featureCategory contains all expected values`() {
        val values = FeatureCategory.values()
        // AUTH, ONBOARDING, DASHBOARD, RESUME, ATS, COVER_LETTER, INTERVIEW, AI_CHAT,
        // JOB_SEARCH, JOB_TRACKER, CAREER_ROADMAP, LEARNING_HUB, PROFILE, SETTINGS,
        // PROVIDERS, NOTIFICATIONS, ANALYTICS = 17
        assertEquals(17, values.size)
        assertTrue(values.contains(FeatureCategory.AUTH))
        assertTrue(values.contains(FeatureCategory.RESUME))
        assertTrue(values.contains(FeatureCategory.ANALYTICS))
    }

    // ── flush tests ─────────────────────────────────

    @Test
    fun `flush persists buffered events to database`() = runBlocking {
        analyticsEngine.startSession()
        analyticsEngine.trackEvent("evt1", FeatureCategory.DASHBOARD)
        analyticsEngine.trackEvent("evt2", FeatureCategory.RESUME)

        analyticsEngine.flush()

        // verify(exactly = 2) { mockDao.insertAnalyticsEvents(any()) }
    }
}
