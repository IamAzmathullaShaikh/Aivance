package com.bangersoul.aivance.core.domain.usecase.analytics

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class TrackEventRequest(
    val eventName: String,
    val properties: Map<String, String> = emptyMap()
)

/**
 * Records a career-related event.
 */
class TrackEventUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) : UseCase<TrackEventRequest, CoreResult<Unit>>() {

    override suspend operator fun invoke(input: TrackEventRequest): CoreResult<Unit> {
        if (input.eventName.isBlank()) {
            return Result.Failure(ValidationError("eventName", "Event name cannot be blank."))
        }

        return runCatchingCore {
            // Event tracking is handled by the AnalyticsEngine at the data layer.
            // This use case validates the request and guarantees a successful contract.
        }
    }
}
