package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class ImproveResumeRequest(
    val resumeId: Long,
    val versionId: Long,
    val jobDescription: String? = null,
    val feedback: String = ""
)

/**
 * Improves a specific resume version using AI.
 */
class ImproveResumeUseCase @Inject constructor(
    private val resumeRepository: ResumeRepository,
    private val aiRepository: AiRepository
) : UseCase<ImproveResumeRequest, CoreResult<ResumeVersion>>() {

    override suspend operator fun invoke(input: ImproveResumeRequest): CoreResult<ResumeVersion> {
        return runCatchingCore {
            val versionsResult = resumeRepository.getVersions(input.resumeId).firstOrNull()
            val versions = (versionsResult as? Result.Success)?.data ?: throw Exception("Failed to fetch versions")
            val original = versions.find { it.id == input.versionId } ?: throw Exception("Version not found")

            val promptBuilder = StringBuilder("Please improve the following resume section for better clarity, impact, and phrasing.")
            if (!input.jobDescription.isNullOrBlank()) {
                promptBuilder.append(" Tailor the content for this job description: ${input.jobDescription}.")
            }
            if (input.feedback.isNotBlank()) {
                promptBuilder.append(" Also consider this feedback: ${input.feedback}.")
            }
            val prompt = promptBuilder.toString()

            val improvedSections = original.sections.map { section ->
                val result = aiRepository.analyzeText(section.content, prompt)
                when (result) {
                    is Result.Success -> section.copy(content = result.data, id = 0, versionId = 0)
                    is Result.Failure -> throw Exception("Failed to improve section ${section.title}: ${result.error.message}")
                }
            }

            val improved = original.copy(
                id = 0, // Force new entry
                versionName = "${original.versionName} (Improved)",
                lastModified = System.currentTimeMillis(),
                sections = improvedSections
            )

            val newVersionId = resumeRepository.saveVersion(improved)
            when (val res = newVersionId) {
                is Result.Success -> improved.copy(id = res.data)
                is Result.Failure -> throw Exception(res.error.message)
            }
        }
    }
}
