package com.bangersoul.aivance.core.domain.usecase.analytics

import com.bangersoul.aivance.core.common.model.AnalyticsEvent
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class TrackEventRequest(
    val eventName: String,
    val category: String = "GENERAL",
    val properties: Map<String, String> = emptyMap()
)

/**
 * Tracks an analytics event.
 *
 * Business rules:
 * - Event name must not be blank.
 * - Events are persisted locally before potential sync.
 * - Events are tagged with a category for filtering.
 */
class TrackEventUseCase @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : UseCase<TrackEventRequest, CoreResult<Unit>>() {

    override suspend operator fun invoke(input: TrackEventRequest): CoreResult<Unit> {
        if (input.eventName.isBlank()) {
            return Result.Failure(ValidationError("eventName", "Event name cannot be blank."))
        }

        return runCatchingCore {
            val event = AnalyticsEvent(
                id = java.util.UUID.randomUUID().toString(),
                eventName = input.eventName,
                category = input.category,
                properties = input.properties
            )

            val result = analyticsRepository.logEvent(event)
            when (result) {
                is Result.Success -> Unit
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }
}
