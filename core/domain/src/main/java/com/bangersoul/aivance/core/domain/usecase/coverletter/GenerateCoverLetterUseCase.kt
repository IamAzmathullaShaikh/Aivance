package com.bangersoul.aivance.core.domain.usecase.coverletter

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class GenerateCoverLetterRequest(
    val resumeId: Long,
    val resumeVersionId: Long,
    val jobId: Long,
    val recruiterId: String? = null,
    val writingStyle: String = "PROFESSIONAL"
)

/**
 * Orchestrates the generation of a personalized cover letter.
 */
class GenerateCoverLetterUseCase @Inject constructor(
    private val coverLetterRepository: CoverLetterRepository
) : UseCase<GenerateCoverLetterRequest, CoreResult<Long>>() {

    override suspend operator fun invoke(input: GenerateCoverLetterRequest): CoreResult<Long> {
        if (input.resumeId <= 0) return com.bangersoul.aivance.core.common.result.Result.Failure(ValidationError("resumeId", "Invalid ID"))
        if (input.jobId <= 0) return com.bangersoul.aivance.core.common.result.Result.Failure(ValidationError("jobId", "Invalid ID"))

        return coverLetterRepository.generateCoverLetter(
            resumeId = input.resumeId,
            resumeVersionId = input.resumeVersionId,
            jobId = input.jobId,
            recruiterId = input.recruiterId,
            writingStyle = input.writingStyle
        )
    }
}
