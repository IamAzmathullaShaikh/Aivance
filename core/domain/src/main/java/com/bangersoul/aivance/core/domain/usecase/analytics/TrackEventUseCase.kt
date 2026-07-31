package com.bangersoul.aivance.core.domain.usecase.analytics

import com.bangersoul.aivance.core.common.result.CoreResult
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

    override suspend operator fun invoke(input: TrackEventRequest): CoreResult<Unit> = runCatchingCore {
        // Log event to repository (if implemented)
        // For now, it might just be a placeholder or we add it to the repo contract.
    }
}
