package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.AtsResult
import com.bangersoul.aivance.core.common.model.ResumeAnalysis
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class AtsScoreRequest(
    val resumeId: Long,
    val jobDescription: String,
    val companyName: String? = null
)

data class AtsScoreResponse(
    val atsResult: AtsResult,
    val analysis: ResumeAnalysis
)

/**
 * Calculates the ATS (Applicant Tracking System) score for a resume.
 *
 * Business rules:
 * - Score is calculated based on keyword matching and formatting analysis.
 * - Score ranges from 0 to 100.
 * - Missing keywords are identified for improvement suggestions.
 * - Formatting issues are evaluated.
 */
class CalculateATSScoreUseCase @Inject constructor(
    private val resumeRepository: ResumeRepository
) : UseCase<AtsScoreRequest, CoreResult<AtsScoreResponse>>() {

    override suspend operator fun invoke(input: AtsScoreRequest): CoreResult<AtsScoreResponse> {
        if (input.resumeId <= 0) {
            return Result.Failure(ValidationError("resumeId", "Invalid resume ID."))
        }
        if (input.jobDescription.isBlank()) {
            return Result.Failure(ValidationError("jobDescription", "Job description cannot be blank."))
        }

        return runCatchingCore {
            val resumeResult = resumeRepository.getResumeById(input.resumeId).firstOrNull()
            val resume = when (resumeResult) {
                is Result.Success -> resumeResult.data
                is Result.Failure -> throw Exception(resumeResult.error.message)
                null -> throw Exception("Resume not found.")
            }

            val analysis = resumeRepository.analyzeResume(input.resumeId, input.jobDescription)
            val analysisData = when (analysis) {
                is Result.Success -> analysis.data
                is Result.Failure -> throw Exception(analysis.error.message)
            }

            val atsResult = AtsResult(
                score = analysisData.overallScore.coerceIn(0, 100),
                resumeName = resume.fileName,
                feedback = analysisData.matchSummary,
                missingKeywords = analysisData.missingKeywords,
                matchingKeywords = analysisData.matchingKeywords,
                formattingScore = calculateFormattingScore(resume.rawText)
            )

            AtsScoreResponse(
                atsResult = atsResult,
                analysis = analysisData
            )
        }
    }

    private fun calculateFormattingScore(text: String): Int {
        var score = 100

        // Check for common formatting issues
        if (text.lines().any { it.length > 150 }) {
            score -= 10
        }

        val bulletPoints = text.count { it == '•' || it == '-' || it == '*' }
        if (bulletPoints == 0 && text.length > 500) {
            score -= 15
        }

        val emailRegex = Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        if (!emailRegex.containsMatchIn(text)) {
            score -= 5
        }

        val phoneRegex = Regex("\\+?[0-9]{7,15}")
        if (!phoneRegex.containsMatchIn(text)) {
            score -= 5
        }

        return score.coerceIn(0, 100)
    }
}
