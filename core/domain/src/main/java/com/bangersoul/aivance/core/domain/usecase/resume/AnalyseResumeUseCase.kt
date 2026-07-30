package com.bangersoul.aivance.core.domain.usecase.resume

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

data class AnalyseResumeRequest(
    val resumeId: Long,
    val jobDescription: String
)

/**
 * Analyses a resume against a job description using AI.
 *
 * Business rules:
 * - Resume must exist and be parsed.
 * - Job description must not be blank.
 * - Returns structured analysis with score, keywords, and suggestions.
 */
class AnalyseResumeUseCase @Inject constructor(
    private val resumeRepository: ResumeRepository
) : UseCase<AnalyseResumeRequest, CoreResult<ResumeAnalysis>>() {

    override suspend operator fun invoke(input: AnalyseResumeRequest): CoreResult<ResumeAnalysis> {
        if (input.resumeId <= 0) {
            return Result.Failure(ValidationError("resumeId", "Invalid resume ID."))
        }
        if (input.jobDescription.isBlank()) {
            return Result.Failure(ValidationError("jobDescription", "Job description cannot be blank."))
        }
        if (input.jobDescription.length < 20) {
            return Result.Failure(ValidationError("jobDescription", "Job description must be at least 20 characters."))
        }

        return runCatchingCore {
            val resumeResult = resumeRepository.getResumeById(input.resumeId).firstOrNull()
            val resume = when (resumeResult) {
                is Result.Success -> resumeResult.data
                is Result.Failure -> throw Exception(resumeResult.error.message)
                null -> throw Exception("Resume not found.")
            }

            if (resume.rawText.isBlank()) {
                throw Exception("Resume has no text content to analyse.")
            }

            val analysis = resumeRepository.analyzeResume(input.resumeId, input.jobDescription)

            when (analysis) {
                is Result.Success -> {
                    val result = analysis.data
                    if (result.overallScore < 0 || result.overallScore > 100) {
                        throw Exception("Invalid analysis score returned by AI.")
                    }
                    result
                }
                is Result.Failure -> throw Exception(analysis.error.message)
            }
        }
    }
}
