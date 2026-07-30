package com.bangersoul.aivance.core.domain.usecase.analytics

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import com.bangersoul.aivance.core.domain.usecase.NoInputUseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class UsageReport(
    val totalEvents: Int = 0,
    val eventsByCategory: Map<String, Int> = emptyMap(),
    val mostFrequentEvent: String? = null
)

/**
 * Generates a usage report from tracked analytics events.
 *
 * Business rules:
 * - Analyzes all stored analytics events.
 * - Categorizes events by type and frequency.
 * - Returns summary statistics about app usage.
 */
class GenerateUsageReportUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : NoInputUseCase<CoreResult<UsageReport>>() {

    override suspend operator fun invoke(): CoreResult<UsageReport> {
        return runCatchingCore {
            val eventsResult = analyticsRepository.getEvents().firstOrNull()
            val events = when (eventsResult) {
                is Result.Success -> eventsResult.data
                is Result.Failure -> emptyList()
                null -> emptyList()
            }

            val eventsByCategory = events.groupBy { it.category }
                .mapValues { it.value.size }

            val mostFrequentEvent = events.groupBy { it.eventName }
                .maxByOrNull { it.value.size }
                ?.key

            UsageReport(
                totalEvents = events.size,
                eventsByCategory = eventsByCategory,
                mostFrequentEvent = mostFrequentEvent
            )
        }
    }
}
