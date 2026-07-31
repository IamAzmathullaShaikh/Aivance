package com.bangersoul.aivance.core.domain.repository.crm

import com.bangersoul.aivance.core.common.model.OutreachDraft
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface OutreachRepository {
    fun getDraftsForRecruiter(recruiterId: String): Flow<CoreResult<List<OutreachDraft>>>
    suspend fun generateDraft(
        resumeId: Long,
        versionId: Long,
        recruiterId: String,
        jobId: String,
        type: String
    ): CoreResult<OutreachDraft>
    suspend fun saveDraft(draft: OutreachDraft): CoreResult<Long>
}
