package com.bangersoul.aivance.core.data.repository.crm

import com.bangersoul.aivance.core.common.model.Recruiter
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.RecruiterDao
import com.bangersoul.aivance.core.domain.repository.crm.RecruiterIntelligenceRepository
import com.bangersoul.aivance.sdk.api.EnrichmentProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecruiterIntelligenceRepositoryImpl @Inject constructor(
    private val recruiterDao: RecruiterDao,
    private val providerManager: ProviderManager
) : RecruiterIntelligenceRepository {

    override fun getRecruitersForCompany(companyId: Long): Flow<CoreResult<List<Recruiter>>> {
        return recruiterDao.getRecruitersForCompany(companyId).map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
    }

    override suspend fun findRecruiters(companyDomain: String): CoreResult<List<Recruiter>> = runCatchingCore {
        val provider = providerManager.getBestProviderFor(ProviderCapability.RecruiterDiscovery) as? EnrichmentProvider
            ?: throw Exception("No enrichment provider configured")

        val result = provider.findRecruiters(companyDomain)
        when (result) {
            is Result.Success -> {
                result.data.forEach { saveRecruiter(it) }
                result.data
            }
            is Result.Failure -> throw Exception(result.error.message)
        }
    }

    override suspend fun saveRecruiter(recruiter: Recruiter): CoreResult<Unit> = runCatchingCore {
        recruiterDao.insertRecruiter(recruiter.toEntity())
        recruiter.contacts.forEach {
            recruiterDao.insertContact(it.toEntity())
        }
    }

    override suspend fun verifyEmail(email: String): CoreResult<Boolean> = runCatchingCore {
        val provider = providerManager.getBestProviderFor(ProviderCapability.EmailVerification) as? EnrichmentProvider
            ?: throw Exception("No verification provider configured")

        val result = provider.verifyEmail(email)
        when (result) {
            is Result.Success -> result.data
            is Result.Failure -> throw Exception(result.error.message)
        }
    }
}
