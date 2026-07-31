package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.job.JobNormalizer
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.CompanyDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.model.CompanyEntity
import com.bangersoul.aivance.core.database.model.SavedJobEntity
import com.bangersoul.aivance.core.database.model.ViewedJobEntity
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.sdk.api.JobProvider
import com.bangersoul.aivance.sdk.infrastructure.ProviderRegistry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val companyDao: CompanyDao,
    private val providerRegistry: ProviderRegistry,
    private val normalizer: JobNormalizer
) : JobRepository {

    override fun getJobs(): Flow<CoreResult<List<JobListing>>> {
        return jobDao.getJobsWithDetails().map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
    }

    override suspend fun searchJobs(
        filter: JobSearchFilter,
        sortOrder: JobSortOrder
    ): CoreResult<List<JobListing>> = coroutineScope {
        runCatchingCore {
            val providers = providerRegistry.getAllProviders()
                .filterIsInstance<JobProvider>()
                .filter { it.status == com.bangersoul.aivance.sdk.core.ProviderStatus.Active ||
                           it.status == com.bangersoul.aivance.sdk.core.ProviderStatus.Ready }

            val deferredResults = providers.map { provider ->
                async {
                    provider.searchJobs(filter, sortOrder, 1).let { result ->
                        when (result) {
                            is Result.Success -> result.data.map { normalizer.normalize(provider.metadata.id, it) }
                            is Result.Failure -> emptyList()
                        }
                    }
                }
            }

            val aggregated = deferredResults.awaitAll().flatten()

            // Cache in background
            aggregated.forEach { job ->
                val companyId = companyDao.insertCompany(CompanyEntity(
                    name = job.company,
                    logoUrl = job.companyLogoUrl,
                    website = null,
                    industry = null,
                    domain = null,
                    headquarters = null,
                    socialLinks = emptyMap()
                ))
                jobDao.insertJob(job.toEntity(companyId))
            }

            aggregated
        }
    }

    override suspend fun getJobById(id: String): CoreResult<JobListing> = runCatchingCore {
        val longId = id.toLongOrNull() ?: throw Exception("Invalid ID")
        jobDao.getJobWithDetailsById(longId)?.toDomain() ?: throw Exception("Job not found")
    }

    override fun getSavedJobs(): Flow<CoreResult<List<JobListing>>> {
        return jobDao.getJobsWithDetails().map { allJobs ->
            val savedIds = jobDao.getSavedJobIds().firstOrNull() ?: emptyList()
            runCatchingCore {
                allJobs.filter { savedIds.contains(it.job.id) }.map { it.toDomain() }
            }
        }
    }

    override suspend fun toggleBookmark(jobId: String): CoreResult<Boolean> = runCatchingCore {
        val id = jobId.toLongOrNull() ?: throw Exception("Invalid ID")
        val isSaved = jobDao.isJobSaved(id)
        if (isSaved) {
            jobDao.deleteSavedJob(SavedJobEntity(id))
            false
        } else {
            jobDao.insertSavedJob(SavedJobEntity(id))
            true
        }
    }

    override suspend fun markAsViewed(jobId: String): CoreResult<Unit> = runCatchingCore {
        val id = jobId.toLongOrNull() ?: throw Exception("Invalid ID")
        jobDao.insertViewedJob(ViewedJobEntity(id))
    }
}
