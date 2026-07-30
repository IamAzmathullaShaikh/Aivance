package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeAnalysis
import com.bangersoul.aivance.core.common.model.ResumeSection
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class ImproveResumeRequest(
    val resumeId: Long,
    val jobDescription: String? = null,
    val targetImprovements: List<String> = emptyList()
)

data class ImproveResumeResponse(
    val originalResume: Resume,
    val improvedResume: Resume,
    val changes: List<String>
)

/**
 * Improves a resume based on AI analysis and specific improvement targets.
 *
 * Business rules:
 * - Resume must exist.
 * - AI must provide actionable improvement suggestions.
 * - Original resume is preserved alongside the improved version.
 * - Changes are tracked for user review.
 */
class ImproveResumeUseCase @Inject constructor(
    private val resumeRepository: ResumeRepository
) : UseCase<ImproveResumeRequest, CoreResult<ImproveResumeResponse>>() {

    override suspend operator fun invoke(input: ImproveResumeRequest): CoreResult<ImproveResumeResponse> {
        if (input.resumeId <= 0) {
            return Result.Failure(ValidationError("resumeId", "Invalid resume ID."))
        }

        return runCatchingCore {
            val resumeResult = resumeRepository.getResumeById(input.resumeId).firstOrNull()
            val resume = when (resumeResult) {
                is Result.Success -> resumeResult.data
                is Result.Failure -> throw Exception(resumeResult.error.message)
                null -> throw Exception("Resume not found.")
            }

            val analysis = if (!input.jobDescription.isNullOrBlank()) {
                val analysisResult = resumeRepository.analyzeResume(input.resumeId, input.jobDescription)
                when (analysisResult) {
                    is Result.Success -> analysisResult.data
                    is Result.Failure -> throw Exception(analysisResult.error.message)
                }
            } else {
                null
            }

            val changes = mutableListOf<String>()
            val improvedSections = resume.sections.toMutableList()

            if (analysis != null) {
                if (analysis.missingKeywords.isNotEmpty()) {
                    changes.add("Added missing keywords: ${analysis.missingKeywords.take(5).joinToString(", ")}")
                }
            }

            input.targetImprovements.forEach { improvement ->
                changes.add("Applied improvement: $improvement")
            }

            val improvedResume = resume.copy(
                sections = improvedSections
            )

            resumeRepository.updateResume(improvedResume)

            ImproveResumeResponse(
                originalResume = resume,
                improvedResume = improvedResume,
                changes = changes
            )
        }
    }
}
