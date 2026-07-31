package com.bangersoul.aivance.core.domain.usecase.coverletter

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class RegenerateSectionRequest(
    val versionId: Long,
    val sectionType: String
)

/**
 * Regenerates a single section of a cover letter draft.
 */
class RegenerateCoverLetterSectionUseCase @Inject constructor(
    private val coverLetterRepository: CoverLetterRepository
) : UseCase<RegenerateSectionRequest, CoreResult<Unit>>() {

    override suspend operator fun invoke(input: RegenerateSectionRequest): CoreResult<Unit> {
        return coverLetterRepository.regenerateSection(input.versionId, input.sectionType)
    }
}
