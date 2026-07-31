package com.bangersoul.aivance.core.domain.usecase.crm

import com.bangersoul.aivance.core.common.model.OutreachDraft
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.domain.repository.crm.OutreachRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class OutreachRequest(
    val resumeId: Long,
    val versionId: Long,
    val recruiterId: String,
    val jobId: String,
    val type: String
)

/**
 * Generates an AI-powered outreach draft.
 */
class GenerateOutreachDraftUseCase @Inject constructor(
    private val outreachRepository: OutreachRepository
) : UseCase<OutreachRequest, CoreResult<OutreachDraft>>() {

    override suspend operator fun invoke(input: OutreachRequest): CoreResult<OutreachDraft> {
        if (input.recruiterId.isBlank()) return com.bangersoul.aivance.core.common.result.Result.Failure(ValidationError("recruiterId", "Missing ID"))

        return outreachRepository.generateDraft(
            resumeId = input.resumeId,
            versionId = input.versionId,
            recruiterId = input.recruiterId,
            jobId = input.jobId,
            type = input.type
        )
    }
}
