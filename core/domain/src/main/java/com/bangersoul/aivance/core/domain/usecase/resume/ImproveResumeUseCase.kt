package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeVersion
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
    val versionId: Long,
    val jobDescription: String? = null,
    val feedback: String = ""
)

/**
 * Improves a specific resume version using AI.
 */
class ImproveResumeUseCase @Inject constructor(
    private val resumeRepository: ResumeRepository
) : UseCase<ImproveResumeRequest, CoreResult<ResumeVersion>>() {

    override suspend operator fun invoke(input: ImproveResumeRequest): CoreResult<ResumeVersion> {
        return runCatchingCore {
            // Placeholder logic for improvement flow
            // In a real implementation, this would call AI and then save a NEW version.
            val versionsResult = resumeRepository.getVersions(input.resumeId).firstOrNull()
            val versions = (versionsResult as? Result.Success)?.data ?: throw Exception("Failed to fetch versions")
            val original = versions.find { it.id == input.versionId } ?: throw Exception("Version not found")

            val improved = original.copy(
                id = 0, // Force new entry
                versionName = "${original.versionName} (Improved)",
                lastModified = System.currentTimeMillis()
            )

            val newVersionId = resumeRepository.saveVersion(improved)
            when (val res = newVersionId) {
                is Result.Success -> improved.copy(id = res.data)
                is Result.Failure -> throw Exception(res.error.message)
            }
        }
    }
}
