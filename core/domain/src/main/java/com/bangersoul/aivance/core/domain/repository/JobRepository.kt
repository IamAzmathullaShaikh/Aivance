package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    fun getJobs(): Flow<CoreResult<List<JobListing>>>
    suspend fun searchJobs(filter: JobSearchFilter, sortOrder: JobSortOrder): CoreResult<List<JobListing>>
    suspend fun getJobById(id: String): CoreResult<JobListing>
    fun getSavedJobs(): Flow<CoreResult<List<JobListing>>>
    suspend fun toggleBookmark(jobId: String): CoreResult<Boolean>
    suspend fun markAsViewed(jobId: String): CoreResult<Unit>
}
