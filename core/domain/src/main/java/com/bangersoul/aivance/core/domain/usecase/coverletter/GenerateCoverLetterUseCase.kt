package com.bangersoul.aivance.core.domain.usecase.coverletter

import com.bangersoul.aivance.core.common.enums.LetterTone
import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class GenerateCoverLetterRequest(
    val companyName: String,
    val role: String,
    val jobDescription: String,
    val tone: LetterTone = LetterTone.PROFESSIONAL,
    val resumeId: Long? = null
)

/**
 * Generates a tailored cover letter using AI.
 *
 * Business rules:
 * - Company name and role must be provided.
 * - Job description is optional but improves quality.
 * - Uses the specified tone for the letter.
 * - Saves the generated letter to local storage.
 */
class GenerateCoverLetterUseCase @Inject constructor(
    private val coverLetterRepository: CoverLetterRepository
) : UseCase<GenerateCoverLetterRequest, CoreResult<CoverLetter>>() {

    override suspend operator fun invoke(input: GenerateCoverLetterRequest): CoreResult<CoverLetter> {
        if (input.companyName.isBlank()) {
            return Result.Failure(ValidationError("companyName", "Company name cannot be blank."))
        }
        if (input.role.isBlank()) {
            return Result.Failure(ValidationError("role", "Job role cannot be blank."))
        }

        return runCatchingCore {
            val response = if (input.resumeId != null && input.resumeId > 0) {
                coverLetterRepository.generateCoverLetter(
                    resumeId = input.resumeId,
                    jobDescription = input.jobDescription.ifBlank { "Create a cover letter for $input.role at $input.companyName" },
                    tone = input.tone
                )
            } else {
                coverLetterRepository.generateCoverLetter(
                    resumeId = 0,
                    jobDescription = input.jobDescription.ifBlank { "Create a cover letter for $input.role at $input.companyName" },
                    tone = input.tone
                )
            }

            when (response) {
                is Result.Success -> response.data
                is Result.Failure -> throw Exception(response.error.message)
            }
        }
    }
}
