package com.bangersoul.aivance.core.domain.usecase.ats

import com.bangersoul.aivance.core.domain.repository.AtsRepository
import com.bangersoul.aivance.core.domain.repository.AtsStreamEvent
import com.bangersoul.aivance.core.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Streams the ATS analysis token-by-token so the UI can render a live preview
 * instead of a blank spinner. Emits [AtsStreamEvent.Chunk] for each token,
 * then a terminal [AtsStreamEvent.Completed] (or [AtsStreamEvent.Failed]).
 */
class StreamAtsAnalysisUseCase @Inject constructor(
    private val atsRepository: AtsRepository
) : FlowUseCase<AtsAnalysisRequest, AtsStreamEvent>() {

    override fun invoke(input: AtsAnalysisRequest): Flow<AtsStreamEvent> {
        return atsRepository.streamAtsAnalysis(input.resumeId, input.versionId, input.jobDescriptionId)
    }
}
