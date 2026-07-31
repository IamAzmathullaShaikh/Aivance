package com.bangersoul.aivance.core.domain.repository.crm

import com.bangersoul.aivance.core.common.model.Recruiter
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface RecruiterIntelligenceRepository {
    fun getRecruitersForCompany(companyId: Long): Flow<CoreResult<List<Recruiter>>>
    suspend fun findRecruiters(companyDomain: String): CoreResult<List<Recruiter>>
    suspend fun saveRecruiter(recruiter: Recruiter): CoreResult<Unit>
    suspend fun verifyEmail(email: String): CoreResult<Boolean>
}
