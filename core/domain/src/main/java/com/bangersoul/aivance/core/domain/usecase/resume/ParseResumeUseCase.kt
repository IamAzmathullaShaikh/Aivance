package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeSection
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class ParseResumeResponse(
    val resume: Resume,
    val sections: List<ResumeSection>
)

/**
 * Parses a resume's raw text into structured sections.
 * Business rules:
 * - Resume must exist and have sufficient text content.
 * - Sections are extracted by identifying common resume section headers.
 * - Preserves the original raw text alongside parsed sections.
 */
class ParseResumeUseCase @Inject constructor(
    private val resumeRepository: ResumeRepository
) : UseCase<Long, CoreResult<ParseResumeResponse>>() {

    override suspend operator fun invoke(resumeId: Long): CoreResult<ParseResumeResponse> {
        if (resumeId <= 0) {
            return Result.Failure(ValidationError("resumeId", "Invalid resume ID."))
        }

        return runCatchingCore {
            // Fetch the resume
            val resumeResult = resumeRepository.getResumeById(resumeId).firstOrNull()
            val resume = when (resumeResult) {
                is Result.Success -> resumeResult.data
                is Result.Failure -> throw Exception(resumeResult.error.message)
                null -> throw Exception("Resume not found.")
            }

            val sections = extractSections(resume.rawText)

            resumeRepository.updateSections(resumeId, sections)

            ParseResumeResponse(
                resume = resume.copy(sections = sections),
                sections = sections
            )
        }
    }

    private fun extractSections(text: String): List<ResumeSection> {
        val sectionHeaders = listOf(
            "SUMMARY" to "SUMMARY|PROFESSIONAL SUMMARY|PROFILE|OBJECTIVE|ABOUT ME",
            "EXPERIENCE" to "EXPERIENCE|WORK EXPERIENCE|PROFESSIONAL EXPERIENCE|EMPLOYMENT|WORK HISTORY",
            "EDUCATION" to "EDUCATION|ACADEMIC BACKGROUND|QUALIFICATIONS|DEGREE",
            "SKILLS" to "SKILLS|TECHNICAL SKILLS|CORE COMPETENCIES|EXPERTISE|SKILLS & EXPERTISE",
            "PROJECTS" to "PROJECTS|PROJECT|KEY PROJECTS",
            "CERTIFICATIONS" to "CERTIFICATIONS|CERTIFICATION|LICENSES|ACCREDITATIONS",
            "LANGUAGES" to "LANGUAGES|LANGUAGE PROFICIENCY",
            "PUBLICATIONS" to "PUBLICATIONS|PUBLICATION|RESEARCH",
            "VOLUNTEERING" to "VOLUNTEERING|VOLUNTEER EXPERIENCE|VOLUNTEER WORK",
            "REFERENCES" to "REFERENCES|REFERENCE"
        )

        val sections = mutableListOf<ResumeSection>()
        val lines = text.lines()
        var currentHeader: String? = null
        var currentContent = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            val matchedHeader = sectionHeaders.firstOrNull { (_, pattern) ->
                Regex(pattern, RegexOption.IGNORE_CASE).matches(trimmed)
            }

            if (matchedHeader != null) {
                // Save previous section
                if (currentHeader != null && currentContent.isNotBlank()) {
                    sections.add(
                        ResumeSection(
                            sectionType = currentHeader,
                            title = currentHeader.lowercase().replaceFirstChar { it.uppercase() },
                            content = currentContent.toString().trim()
                        )
                    )
                }
                currentHeader = matchedHeader.first
                currentContent = StringBuilder()
            } else {
                if (currentContent.length > 0) currentContent.append("\n")
                currentContent.append(line)
            }
        }

        // Save last section
        if (currentHeader != null && currentContent.isNotBlank()) {
            sections.add(
                ResumeSection(
                    sectionType = currentHeader,
                    title = currentHeader.lowercase().replaceFirstChar { it.uppercase() },
                    content = currentContent.toString().trim()
                )
            )
        }

        return sections
    }
}
