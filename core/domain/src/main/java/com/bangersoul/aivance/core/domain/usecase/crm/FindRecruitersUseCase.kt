package com.bangersoul.aivance.core.domain.usecase.crm

import com.bangersoul.aivance.core.common.model.Recruiter
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.repository.crm.RecruiterIntelligenceRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

/**
 * Discovers recruiters for a company domain.
 */
class FindRecruitersUseCase @Inject constructor(
    private val recruiterRepository: RecruiterIntelligenceRepository
) : UseCase<String, CoreResult<List<Recruiter>>>() {

    override suspend operator fun invoke(input: String): CoreResult<List<Recruiter>> {
        return recruiterRepository.findRecruiters(input)
    }
}
