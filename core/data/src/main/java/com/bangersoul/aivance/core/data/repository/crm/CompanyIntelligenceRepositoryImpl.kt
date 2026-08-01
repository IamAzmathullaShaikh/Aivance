package com.bangersoul.aivance.core.data.repository.crm

import com.bangersoul.aivance.core.common.model.Company
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.CompanyDao
import com.bangersoul.aivance.core.domain.repository.crm.CompanyIntelligenceRepository
import com.bangersoul.aivance.sdk.api.EnrichmentProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyIntelligenceRepositoryImpl @Inject constructor(
    private val companyDao: CompanyDao,
    private val providerManager: ProviderManager
) : CompanyIntelligenceRepository {

    override fun getCompanies(): Flow<CoreResult<List<Company>>> {
        return companyDao.getCompanies().map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
    }

    override suspend fun getCompanyById(id: String): Company? {
        val longId = id.toLongOrNull() ?: return null
        return companyDao.getCompanyById(longId)?.toDomain()
    }

    override suspend fun getCompanyByName(name: String): Company? {
        return companyDao.getCompanyByName(name)?.toDomain()
    }

    override suspend fun saveCompany(company: Company): CoreResult<Long> = runCatchingCore {
        companyDao.insertCompany(company.toEntity())
    }

    override suspend fun enrichCompany(companyId: String): CoreResult<Company> = runCatchingCore {
        val company = getCompanyById(companyId) ?: throw Exception("Company not found")
        val provider = providerManager.getBestProviderFor(ProviderCapability.RecruiterDiscovery) as? EnrichmentProvider
            ?: throw Exception("No enrichment provider configured")

        val result = provider.enrichCompany(company)
        when (result) {
            is Result.Success -> {
                saveCompany(result.data)
                result.data
            }
            is Result.Failure -> throw Exception(result.error.message)
        }
    }
}
