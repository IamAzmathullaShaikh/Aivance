package com.bangersoul.aivance.core.domain.usecase.ats

import com.bangersoul.aivance.core.common.model.AtsReport
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AtsRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class AtsAnalysisRequest(
    val resumeId: Long,
    val versionId: Long,
    val jobDescriptionId: Long
)

/**
 * Orchestrates the ATS analysis process.
 */
class PerformAtsAnalysisUseCase @Inject constructor(
    private val atsRepository: AtsRepository
) : UseCase<AtsAnalysisRequest, CoreResult<AtsReport>>() {

    override suspend operator fun invoke(input: AtsAnalysisRequest): CoreResult<AtsReport> {
        if (input.resumeId <= 0) return com.bangersoul.aivance.core.common.result.Result.Failure(ValidationError("resumeId", "Invalid ID"))
        if (input.versionId <= 0) return com.bangersoul.aivance.core.common.result.Result.Failure(ValidationError("versionId", "Invalid ID"))
        if (input.jobDescriptionId <= 0) return com.bangersoul.aivance.core.common.result.Result.Failure(ValidationError("jobDescriptionId", "Invalid ID"))

        return atsRepository.performAtsAnalysis(input.resumeId, input.versionId, input.jobDescriptionId)
    }
}
