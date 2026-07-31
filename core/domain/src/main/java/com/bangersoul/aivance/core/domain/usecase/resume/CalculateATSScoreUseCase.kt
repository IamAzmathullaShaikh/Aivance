package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.AtsResult
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeAnalysis
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class AtsScoreRequest(
    val resumeId: Long,
    val versionId: Long,
    val jobDescription: String,
    val companyName: String? = null
)

data class AtsScoreResponse(
    val atsResult: AtsResult,
    val analysis: ResumeAnalysis
)

/**
 * Calculates the ATS score for a specific resume version.
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

            val analysis = resumeRepository.analyzeResume(input.resumeId, input.versionId, input.jobDescription)
            val analysisData = when (analysis) {
                is Result.Success -> analysis.data
                is Result.Failure -> throw Exception(analysis.error.message)
            }

            val atsResult = AtsResult(
                score = analysisData.overallScore.coerceIn(0, 100),
                resumeName = resume.name,
                feedback = analysisData.matchSummary,
                missingKeywords = analysisData.missingKeywords,
                matchingKeywords = analysisData.matchingKeywords,
                formattingScore = calculateFormattingScore(resume.rawText ?: "")
            )

            AtsScoreResponse(
                atsResult = atsResult,
                analysis = analysisData
            )
        }
    }

    private fun calculateFormattingScore(text: String): Int {
        var score = 100
        if (text.isBlank()) return 0

        if (text.lines().any { it.length > 150 }) score -= 10
        val bulletPoints = text.count { it == '•' || it == '-' || it == '*' }
        if (bulletPoints == 0 && text.length > 500) score -= 15

        return score.coerceIn(0, 100)
    }
}
