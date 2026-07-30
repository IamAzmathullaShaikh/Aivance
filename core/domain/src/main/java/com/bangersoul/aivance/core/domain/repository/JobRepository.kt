package com.bangersoul.aivance.core.domain.repository

import androidx.paging.PagingData
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.SearchFilter
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface JobRepository {
    fun searchJobs(query: String, filter: SearchFilter): Flow<PagingData<JobListing>>
    fun getJobById(id: String): Flow<CoreResult<JobListing>>
}
