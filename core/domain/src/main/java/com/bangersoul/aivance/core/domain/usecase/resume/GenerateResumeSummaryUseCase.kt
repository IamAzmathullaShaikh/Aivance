package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class ResumeSummaryRequest(
    val resumeId: Long,
    val maxLength: Int = 500
)

/**
 * Generates a concise summary of a resume.
 *
 * Business rules:
 * - Resume must exist.
 * - Summary length must not exceed the specified maximum.
 * - Extracts key skills and experience highlights.
 */
class GenerateResumeSummaryUseCase @Inject constructor(
    private val resumeRepository: ResumeRepository
) : UseCase<ResumeSummaryRequest, CoreResult<String>>() {

    override suspend operator fun invoke(input: ResumeSummaryRequest): CoreResult<String> {
        if (input.resumeId <= 0) {
            return Result.Failure(ValidationError("resumeId", "Invalid resume ID."))
        }
        if (input.maxLength <= 0 || input.maxLength > 10000) {
            return Result.Failure(ValidationError("maxLength", "Max length must be between 1 and 10,000."))
        }

        return runCatchingCore {
            val resumeResult = resumeRepository.getResumeById(input.resumeId).firstOrNull()
            val resume = when (resumeResult) {
                is Result.Success -> resumeResult.data
                is Result.Failure -> throw Exception(resumeResult.error.message)
                null -> throw Exception("Resume not found.")
            }

            val text = resume.rawText ?: throw Exception("Resume has no text content.")
            if (text.isBlank()) {
                throw Exception("Resume has no text content.")
            }

            generateSummary(text, input.maxLength)
        }
    }

    private fun generateSummary(text: String, maxLength: Int): String {
        val lines = text.lines().filter { it.isNotBlank() }

        val summary = buildString {
            val sections = listOf("SUMMARY", "PROFILE", "OBJECTIVE")
            for (line in lines) {
                if (sections.any { line.trim().uppercase().startsWith(it) }) {
                    appendLine(line.trim())
                }
            }

            val skillsSection = extractSection(lines, "SKILLS")
            if (skillsSection.isNotBlank()) {
                appendLine("Skills: ${skillsSection.take(200)}")
            }

            val experienceSection = extractSection(lines, "EXPERIENCE")
            if (experienceSection.isNotBlank()) {
                appendLine("Experience: ${experienceSection.take(200)}")
            }
        }

        return if (summary.length > maxLength) {
            summary.take(maxLength - 3) + "..."
        } else {
            summary
        }
    }

    private fun extractSection(lines: List<String>, sectionName: String): String {
        val sectionHeaders = listOf(
            sectionName,
            "$sectionName:",
            "$sectionName "
        )

        var capture = false
        val content = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim().uppercase()
            if (sectionHeaders.any { trimmed.startsWith(it) }) {
                capture = true
                continue
            }
            if (capture) {
                val nextSectionHeaders = listOf(
                    "EDUCATION", "EXPERIENCE", "SKILLS", "PROJECTS",
                    "CERTIFICATIONS", "SUMMARY", "PROFILE", "OBJECTIVE"
                )
                if (nextSectionHeaders.any { trimmed.startsWith(it) } && trimmed != sectionName) {
                    break
                }
                content.appendLine(line.trim())
            }
        }

        return content.toString().trim()
    }
}
