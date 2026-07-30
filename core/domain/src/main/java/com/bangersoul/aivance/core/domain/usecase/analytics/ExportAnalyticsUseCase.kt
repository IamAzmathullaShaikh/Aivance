package com.bangersoul.aivance.core.domain.usecase.analytics

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import com.bangersoul.aivance.core.domain.usecase.NoInputUseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Exports all analytics data as a formatted report.
 *
 * Business rules:
 * - Exports all stored analytics events.
 * - Includes event names, categories, and timestamps.
 * - Returns data in a human-readable format.
 */
class ExportAnalyticsUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : NoInputUseCase<CoreResult<String>>() {

    override suspend operator fun invoke(): CoreResult<String> {
        return runCatchingCore {
            val eventsResult = analyticsRepository.getEvents().firstOrNull()
            val events = when (eventsResult) {
                is Result.Success -> eventsResult.data
                is Result.Failure -> emptyList()
                null -> emptyList()
            }

            val dateFormat = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()
            )

            buildString {
                appendLine("=== Analytics Export ===")
                appendLine("Total Events: ${events.size}")
                appendLine("Export Date: ${dateFormat.format(java.util.Date(System.currentTimeMillis()))}")
                appendLine()

                if (events.isNotEmpty()) {
                    appendLine("--- Events by Category ---")
                    events.groupBy { it.category }.forEach { (category, categoryEvents) ->
                        appendLine("[$category]: ${categoryEvents.size} events")
                    }
                    appendLine()

                    appendLine("--- Timeline ---")
                    events.sortedBy { it.timestamp }.forEach { event ->
                        appendLine("[${dateFormat.format(java.util.Date(event.timestamp))}] ${event.eventName} (${event.category})")
                        if (event.properties.isNotEmpty()) {
                            event.properties.forEach { (key, value) ->
                                appendLine("  $key: $value")
                            }
                        }
                    }
                } else {
                    appendLine("No analytics events recorded.")
                }
            }
        }
    }
}
