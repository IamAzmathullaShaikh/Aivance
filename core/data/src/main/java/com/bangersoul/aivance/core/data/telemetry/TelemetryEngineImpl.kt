package com.bangersoul.aivance.core.data.telemetry

import com.bangersoul.aivance.core.common.dto.PerformanceMetric
import com.bangersoul.aivance.core.common.dto.TraceContext
import com.bangersoul.aivance.core.domain.telemetry.LogEntry
import com.bangersoul.aivance.core.domain.telemetry.LogLevel
import com.bangersoul.aivance.core.domain.telemetry.TelemetryEngine
import timber.log.Timber
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [TelemetryEngine].
 *
 * Features:
 * - Trace/span management for distributed tracing
 * - Structured logging with trace context propagation
 * - Performance metric buffering
 * - Thread-safe log buffer for batch processing
 */
@Singleton
class TelemetryEngineImpl @Inject constructor() : TelemetryEngine {

    private val activeTraces = mutableMapOf<String, TraceContext>()
    private val logBuffer = ConcurrentLinkedQueue<LogEntry>()
    private val metricBuffer = ConcurrentLinkedQueue<PerformanceMetric>()
    private val traceLock = Any()

    // ── Trace Management ─────────────────────────────

    override fun startTrace(operationName: String, tags: Map<String, String>): TraceContext {
        val context = TraceContext(
            traceId = generateTraceId(),
            spanId = generateSpanId(),
            operationName = operationName,
            tags = tags
        )
        synchronized(traceLock) { activeTraces[context.traceId] = context }
        Timber.tag("Trace").d("Started: %s [%s]", operationName, context.traceId)
        return context
    }

    override fun endTrace(context: TraceContext) {
        val ended = context.copy(endTime = System.currentTimeMillis())
        synchronized(traceLock) { activeTraces.remove(context.traceId) }
        val duration = (ended.endTime ?: 0L) - context.startTime
        Timber.tag("Trace").d("Ended: %s [%s] — %dms", context.operationName, context.traceId, duration)
        recordMetric(PerformanceMetric(name = "trace.${context.operationName}.duration",
            value = duration.toDouble(), unit = "ms",
            tags = mapOf("traceId" to context.traceId, "operation" to context.operationName)))
    }

    override fun startSpan(context: TraceContext, operationName: String): TraceContext {
        val span = context.copy(spanId = generateSpanId(), parentSpanId = context.spanId,
            operationName = operationName, startTime = System.currentTimeMillis())
        Timber.tag("Trace").d("  Span: %s [%s] parent=%s", operationName, span.spanId, span.parentSpanId)
        return span
    }

    override fun endSpan(context: TraceContext) {
        val duration = System.currentTimeMillis() - context.startTime
        recordMetric(PerformanceMetric(name = "span.${context.operationName}.duration",
            value = duration.toDouble(), unit = "ms",
            tags = mapOf("traceId" to context.traceId, "spanId" to context.spanId)))
    }

    // ── Structured Logging ───────────────────────────

    override fun log(entry: LogEntry) {
        val tag = entry.tag
        val msg = buildLogMessage(entry)

        when (entry.level) {
            LogLevel.VERBOSE -> Timber.tag(tag).v(entry.throwable, msg)
            LogLevel.DEBUG -> Timber.tag(tag).d(entry.throwable, msg)
            LogLevel.INFO -> Timber.tag(tag).i(entry.throwable, msg)
            LogLevel.WARN -> Timber.tag(tag).w(entry.throwable, msg)
            LogLevel.ERROR -> Timber.tag(tag).e(entry.throwable, msg)
            LogLevel.FATAL -> Timber.tag(tag).wtf(entry.throwable, msg)
        }

        logBuffer.add(entry)
        if (logBuffer.size > 1000) { repeat(100) { logBuffer.poll() } }
    }

    override fun recordMetric(metric: PerformanceMetric) {
        metricBuffer.add(metric)
        Timber.tag("Metrics").d("%s = %.2f %s", metric.name, metric.value, metric.unit)
        if (metricBuffer.size > 500) { repeat(50) { metricBuffer.poll() } }
    }

    override suspend fun flush() {
        logBuffer.clear()
        metricBuffer.clear()
    }

    fun bufferedLogCount(): Int = logBuffer.size
    fun bufferedMetricCount(): Int = metricBuffer.size

    private fun buildLogMessage(entry: LogEntry): String {
        val sb = StringBuilder(entry.message)
        if (entry.traceId != null || entry.spanId != null) {
            sb.append(" [")
            if (entry.traceId != null) sb.append("trace=").append(entry.traceId)
            if (entry.spanId != null) {
                if (entry.traceId != null) sb.append(" ")
                sb.append("span=").append(entry.spanId)
            }
            sb.append("]")
        }
        if (entry.properties.isNotEmpty()) {
            sb.append(" {")
            entry.properties.entries.forEachIndexed { i, (k, v) ->
                if (i > 0) sb.append(", "); sb.append(k).append("=").append(v)
            }
            sb.append("}")
        }
        return sb.toString()
    }
}
