package com.bangersoul.aivance.core.data.repository.crm

import com.bangersoul.aivance.core.common.model.CommunicationHistory
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.RecruiterDao
import com.bangersoul.aivance.core.domain.repository.crm.CRMRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CRMRepositoryImpl @Inject constructor(
    private val recruiterDao: RecruiterDao
) : CRMRepository {

    override fun getHistoryForRecruiter(recruiterId: String): Flow<CoreResult<List<CommunicationHistory>>> {
        return recruiterDao.getHistoryForRecruiter(recruiterId).map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
    }

    override suspend fun logCommunication(history: CommunicationHistory): CoreResult<Long> = runCatchingCore {
        recruiterDao.insertHistory(history.toEntity())
    }

    override suspend fun updateRelationshipStatus(recruiterId: String, status: String): CoreResult<Unit> = runCatchingCore {
        val recruiter = recruiterDao.getRecruiterById(recruiterId) ?: throw Exception("Recruiter not found")
        recruiterDao.insertRecruiter(recruiter.copy(status = status))
    }
}
