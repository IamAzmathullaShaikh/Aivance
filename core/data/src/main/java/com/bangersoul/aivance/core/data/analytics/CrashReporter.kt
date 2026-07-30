package com.bangersoul.aivance.core.data.analytics

import com.bangersoul.aivance.core.domain.analytics.AnalyticsEngine
import com.bangersoul.aivance.core.domain.telemetry.LogLevel
import com.bangersoul.aivance.core.domain.telemetry.LogEntry
import com.bangersoul.aivance.core.domain.telemetry.TelemetryEngine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Abstraction over crash reporting.
 *
 * Respects user consent before recording crashes and non-fatal errors.
 * Falls back to Timber logging when Crashlytics is unavailable.
 */
@Singleton
class CrashReporter @Inject constructor(
    private val analyticsEngine: AnalyticsEngine,
    private val telemetryEngine: TelemetryEngine
) {

    fun recordException(throwable: Throwable, context: Map<String, String> = emptyMap()) {
        if (!analyticsEngine.getConsent().crashReportingEnabled) {
            Timber.w("Crash reporting disabled — suppressed: %s", throwable.message)
            return
        }

        telemetryEngine.log(
            LogEntry(
                level = LogLevel.ERROR,
                message = "Non-fatal: ${throwable.message}",
                tag = "CrashReporter",
                throwable = throwable,
                properties = context
            )
        )
        Timber.e(throwable, "Non-fatal exception recorded")
    }

    fun logBreadcrumb(name: String, attributes: Map<String, String> = emptyMap()) {
        if (!analyticsEngine.getConsent().crashReportingEnabled) return
        Timber.tag("Breadcrumb").d("%s %s", name, attributes)
    }

    fun setUserId(userId: String) {
        if (!analyticsEngine.getConsent().crashReportingEnabled) return
    }

    fun recordNetworkRequest(url: String, method: String, statusCode: Int, durationMs: Long) {
        logBreadcrumb("network_request", mapOf(
            "url" to url,
            "method" to method,
            "status" to statusCode.toString(),
            "duration" to "$durationMs ms"
        ))
    }
}
