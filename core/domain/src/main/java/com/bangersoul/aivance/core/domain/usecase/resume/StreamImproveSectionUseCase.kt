package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class StreamImproveSectionRequest(
    val resumeId: Long,
    val versionId: Long,
    val sectionTitle: String,
    val jobDescription: String? = null,
    val feedback: String = ""
)

/**
 * Streaming improvement of a single resume section — mirrors
 * [ImproveResumeUseCase] but emits token chunks in real time so the Resume
 * Engine can render a live typewriter suggestion. Falls back to a single
 * emission for one-shot providers.
 */
class StreamImproveSectionUseCase @Inject constructor(
    private val resumeRepository: ResumeRepository,
    private val aiRepository: AiRepository
) {
    fun stream(input: StreamImproveSectionRequest): Flow<String> = flow {
        val versionsResult = resumeRepository.getVersions(input.resumeId).firstOrNull()
        val versions = (versionsResult as? Result.Success)?.data
            ?: throw Exception("Failed to fetch versions")
        val original = versions.find { it.id == input.versionId }
            ?: versions.firstOrNull()
            ?: throw Exception("No resume version found to improve")
        val section = original.sections.firstOrNull { it.title.equals(input.sectionTitle, ignoreCase = true) }
            ?: original.sections.firstOrNull()
            ?: throw Exception("Section '${input.sectionTitle}' not found")

        val promptBuilder = StringBuilder(
            "Please improve the following resume section for better clarity, impact, and phrasing."
        )
        if (!input.jobDescription.isNullOrBlank()) {
            promptBuilder.append(" Tailor the content for this job description: ${input.jobDescription}.")
        }
        if (input.feedback.isNotBlank()) {
            promptBuilder.append(" Also consider this feedback: ${input.feedback}.")
        }

        aiRepository.streamAnalyzeText(section.content, promptBuilder.toString())
            .collect { chunk -> emit(chunk) }
    }
}
