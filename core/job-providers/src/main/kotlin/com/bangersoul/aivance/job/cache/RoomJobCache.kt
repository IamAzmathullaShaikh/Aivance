package com.bangersoul.aivance.job.cache

import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.database.dao.CompanyDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.model.CompanyEntity
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomJobCache @Inject constructor(
    private val jobDao: JobDao,
    private val companyDao: CompanyDao
) : JobCache {

    override suspend fun getJobs(): List<JobListing> {
        return jobDao.getJobsWithDetails().first().map { it.toDomain() }
    }

    override suspend fun saveJobs(jobs: List<JobListing>) {
        jobs.forEach { job ->
            val existingCompany = companyDao.getCompanyByName(job.company)
            val companyId = if (existingCompany != null) {
                existingCompany.id
            } else {
                companyDao.insertCompany(
                    CompanyEntity(
                        name = job.company,
                        logoUrl = job.companyLogoUrl,
                        website = null,
                        industry = null,
                        domain = null,
                        headquarters = null,
                        socialLinks = emptyMap()
                    )
                )
            }
            jobDao.insertJob(job.toEntity(companyId))
        }
    }

    override suspend fun clear() {
        jobDao.deleteAllJobs()
    }
}
