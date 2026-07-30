package com.bangersoul.aivance.core.data.telemetry

import com.bangersoul.aivance.core.domain.telemetry.LogEntry
import com.bangersoul.aivance.core.domain.telemetry.LogLevel
import com.bangersoul.aivance.core.domain.telemetry.TelemetryEngine
import timber.log.Timber

/**
 * A structured [Timber.Tree] that pipes log messages into the [TelemetryEngine]
 * for structured logging with trace context, correlation IDs, and buffered output.
 */
class StructuredTimberTree(
    private val telemetryEngine: TelemetryEngine
) : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean = true

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val level = when (priority) {
            android.util.Log.VERBOSE -> LogLevel.VERBOSE
            android.util.Log.DEBUG -> LogLevel.DEBUG
            android.util.Log.INFO -> LogLevel.INFO
            android.util.Log.WARN -> LogLevel.WARN
            android.util.Log.ERROR -> LogLevel.ERROR
            android.util.Log.ASSERT -> LogLevel.FATAL
            else -> LogLevel.DEBUG
        }

        val entry = LogEntry(
            level = level,
            message = message,
            tag = tag ?: "Aivance",
            throwable = t
        )
        telemetryEngine.log(entry)
    }
}
