package com.bangersoul.aivance.core.domain.usecase.resume

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.repository.ResumeRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

/**
 * High-level use case to parse an existing resume.
 */
class ParseResumeUseCase @Inject constructor(
    private val resumeRepository: ResumeRepository
) : UseCase<Long, CoreResult<Unit>>() {

    override suspend operator fun invoke(resumeId: Long): CoreResult<Unit> {
        return resumeRepository.parseResume(resumeId)
    }
}
