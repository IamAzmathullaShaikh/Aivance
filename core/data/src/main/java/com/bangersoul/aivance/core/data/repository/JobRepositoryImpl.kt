package com.bangersoul.aivance.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.model.SearchFilter
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.source.JobLocalDataSource
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.sdk.api.JobProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class JobRepositoryImpl @Inject constructor(
    private val localDataSource: JobLocalDataSource,
    private val jobDao: JobDao,
    private val providerManager: ProviderManager
) : JobRepository {

    override fun searchJobs(query: String, filter: SearchFilter): Flow<PagingData<JobListing>> {
        val provider = providerManager.getBestProviderFor(ProviderCapability.JobSearch) as? JobProvider

        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { jobDao.getJobsPagingSource() }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun getJobById(id: String): Flow<CoreResult<JobListing>> {
        return localDataSource.getJobs().map { jobs ->
            runCatchingCore { jobs.find { it.id == id } ?: throw Exception("Job not found") }
        }
    }

    private fun SearchFilter.toJobSearchFilter(): JobSearchFilter {
        return JobSearchFilter(
            query = keywords,
            location = location,
            minSalary = minSalary?.toDouble(),
            maxSalary = maxSalary?.toDouble()
        )
    }
}
