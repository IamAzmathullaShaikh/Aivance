package com.bangersoul.aivance.core.domain.repository.crm

import com.bangersoul.aivance.core.common.model.CommunicationHistory
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface CRMRepository {
    fun getHistoryForRecruiter(recruiterId: String): Flow<CoreResult<List<CommunicationHistory>>>
    suspend fun logCommunication(history: CommunicationHistory): CoreResult<Long>
    suspend fun updateRelationshipStatus(recruiterId: String, status: String): CoreResult<Unit>
}
