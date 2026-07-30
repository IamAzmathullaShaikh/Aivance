package com.bangersoul.aivance.core.domain.repository

import androidx.paging.PagingData
import com.bangersoul.aivance.core.common.model.JobApplication
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface JobTrackerRepository {
    fun getApplications(): Flow<PagingData<JobApplication>>
    fun getApplicationById(id: Long): Flow<CoreResult<JobApplication>>
    suspend fun insertApplication(application: JobApplication): CoreResult<Long>
    suspend fun updateApplication(application: JobApplication): CoreResult<Unit>
    suspend fun deleteApplication(id: Long): CoreResult<Unit>
}
