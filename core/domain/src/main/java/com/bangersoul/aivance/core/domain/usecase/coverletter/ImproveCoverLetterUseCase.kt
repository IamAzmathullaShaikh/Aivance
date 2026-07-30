package com.bangersoul.aivance.core.domain.usecase.coverletter

import com.bangersoul.aivance.core.common.model.CoverLetter
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class ImproveCoverLetterRequest(
    val coverLetterId: Long,
    val feedback: String = ""
)

/**
 * Improves an existing cover letter based on user feedback or AI suggestions.
 *
 * Business rules:
 * - Cover letter must exist.
 * - Preserves the original version.
 * - Uses AI to apply improvements based on feedback.
 */
class ImproveCoverLetterUseCase @Inject constructor(
    private val coverLetterRepository: CoverLetterRepository
) : UseCase<ImproveCoverLetterRequest, CoreResult<CoverLetter>>() {

    override suspend operator fun invoke(input: ImproveCoverLetterRequest): CoreResult<CoverLetter> {
        if (input.coverLetterId <= 0) {
            return Result.Failure(ValidationError("coverLetterId", "Invalid cover letter ID."))
        }

        return runCatchingCore {
            val letterResult = coverLetterRepository.getCoverLetterById(input.coverLetterId).firstOrNull()
            val letter = when (letterResult) {
                is Result.Success -> letterResult.data
                is Result.Failure -> throw Exception(letterResult.error.message)
                null -> throw Exception("Cover letter not found.")
            }

            val improvementsPrompt = if (input.feedback.isNotBlank()) {
                "Improve this cover letter based on: ${input.feedback}"
            } else {
                "Improve this cover letter's tone and professionalism"
            }

            val improvedLetter = coverLetterRepository.generateCoverLetter(
                resumeId = 0,
                jobDescription = improvementsPrompt,
                tone = letter.tone
            )

            when (improvedLetter) {
                is Result.Success -> improvedLetter.data
                is Result.Failure -> throw Exception(improvedLetter.error.message)
            }
        }
    }
}
