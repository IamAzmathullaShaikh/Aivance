package com.bangersoul.aivance.core.domain.repository.crm

import com.bangersoul.aivance.core.common.model.Company
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface CompanyIntelligenceRepository {
    fun getCompanies(): Flow<CoreResult<List<Company>>>
    suspend fun getCompanyById(id: String): Company?
    suspend fun getCompanyByName(name: String): Company?
    suspend fun saveCompany(company: Company): CoreResult<Long>
    suspend fun enrichCompany(companyId: String): CoreResult<Company>
}
