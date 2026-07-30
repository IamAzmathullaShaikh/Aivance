package com.bangersoul.aivance.core.domain.telemetry

import com.bangersoul.aivance.core.common.dto.PerformanceMetric
import com.bangersoul.aivance.core.common.dto.TraceContext
import java.util.UUID

/**
 * Log severity level.
 */
enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    FATAL
}

/**
 * Structured log entry with trace context.
 */
data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val message: String,
    val tag: String = "Aivance",
    val traceId: String? = null,
    val spanId: String? = null,
    val throwable: Throwable? = null,
    val properties: Map<String, String> = emptyMap()
)

/**
 * Core telemetry engine responsible for:
 * - Generating and propagating correlation IDs (trace IDs)
 * - Structured logging with trace context
 * - Performance metrics collection
 * - Trace/span management for distributed tracing
 */
interface TelemetryEngine {

    /**
     * Generates a new trace ID for correlating related operations.
     */
    fun generateTraceId(): String = UUID.randomUUID().toString().take(16)

    /**
     * Generates a new span ID within a trace.
     */
    fun generateSpanId(): String = UUID.randomUUID().toString().take(8)

    /**
     * Starts a new trace with the given [operationName].
     * Returns a [TraceContext] that should be passed through the operation chain.
     */
    fun startTrace(operationName: String, tags: Map<String, String> = emptyMap()): TraceContext

    /**
     * Ends a trace, recording its duration.
     */
    fun endTrace(context: TraceContext)

    /**
     * Creates a child span within an existing trace context.
     */
    fun startSpan(context: TraceContext, operationName: String): TraceContext

    /**
     * Ends a span.
     */
    fun endSpan(context: TraceContext)

    /**
     * Logs a structured log entry.
     */
    fun log(entry: LogEntry)

    /**
     * Convenience method to log at INFO level.
     */
    fun info(tag: String, message: String, properties: Map<String, String> = emptyMap()) {
        log(LogEntry(level = LogLevel.INFO, tag = tag, message = message, properties = properties))
    }

    /**
     * Convenience method to log at WARN level.
     */
    fun warn(tag: String, message: String, throwable: Throwable? = null, properties: Map<String, String> = emptyMap()) {
        log(LogEntry(level = LogLevel.WARN, tag = tag, message = message, throwable = throwable, properties = properties))
    }

    /**
     * Convenience method to log at ERROR level.
     */
    fun error(tag: String, message: String, throwable: Throwable? = null, properties: Map<String, String> = emptyMap()) {
        log(LogEntry(level = LogLevel.ERROR, tag = tag, message = message, throwable = throwable, properties = properties))
    }

    /**
     * Records a performance metric.
     */
    fun recordMetric(metric: PerformanceMetric)

    /**
     * Flushes any buffered log entries or metrics.
     */
    suspend fun flush()
}
