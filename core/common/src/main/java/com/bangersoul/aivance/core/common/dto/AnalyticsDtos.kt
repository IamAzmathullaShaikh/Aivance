package com.bangersoul.aivance.core.common.dto

import kotlinx.serialization.Serializable

/**
 * Represents a tracked app session.
 */
@Serializable
data class SessionInfo(
    val sessionId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val durationMs: Long? = null,
    val screenViews: List<String> = emptyList(),
    val eventsCount: Int = 0
)

/**
 * System performance metric collected at a point in time.
 */
@Serializable
data class PerformanceMetric(
    val name: String,
    val value: Double,
    val unit: String = "ms",
    val tags: Map<String, String> = emptyMap(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Trace context for correlating operations across boundaries.
 */
@Serializable
data class TraceContext(
    val traceId: String,
    val spanId: String,
    val parentSpanId: String? = null,
    val operationName: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val tags: Map<String, String> = emptyMap()
)

/**
 * User consent preferences for analytics and telemetry.
 */
@Serializable
data class ConsentPreferences(
    val analyticsEnabled: Boolean = true,
    val crashReportingEnabled: Boolean = true,
    val performanceMetricsEnabled: Boolean = true,
    val personalizationEnabled: Boolean = true,
    val thirdPartySharingEnabled: Boolean = false
)

/**
 * Aggregated provider metric for the health dashboard.
 */
@Serializable
data class ProviderMetric(
    val providerId: String,
    val totalCalls: Int = 0,
    val successCount: Int = 0,
    val failureCount: Int = 0,
    val avgLatencyMs: Double = 0.0,
    val p95LatencyMs: Double = 0.0,
    val lastCallTime: Long? = null,
    val isHealthy: Boolean = true
)

/**
 * Application health summary for the health dashboard.
 */
@Serializable
data class HealthSummary(
    val databaseHealthy: Boolean = true,
    val storageHealthy: Boolean = true,
    val networkAvailable: Boolean = false,
    val providersHealthy: Int = 0,
    val providersTotal: Int = 0,
    val memoryUsagePercent: Double = 0.0,
    val lastSyncTime: Long? = null,
    val pendingOperations: Int = 0,
    val sessionCount: Int = 0,
    val crashFreeRate: Double = 100.0
)
