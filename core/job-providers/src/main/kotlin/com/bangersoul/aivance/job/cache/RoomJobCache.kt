package com.bangersoul.aivance.job.cache

import com.bangersoul.aivance.core.common.enums.EmploymentType
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.database.dao.CompanyDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.model.CompanyEntity
import com.bangersoul.aivance.core.database.model.JobEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomJobCache @Inject constructor(
    private val jobDao: JobDao,
    private val companyDao: CompanyDao
) : JobCache {

    override suspend fun getJobs(): List<JobListing> {
        return jobDao.getJobsWithDetails().first().map { entity ->
            JobListing(
                id = entity.job.id.toString(),
                title = entity.job.title,
                company = entity.company.name,
                companyLogoUrl = entity.company.logoUrl,
                location = entity.job.location ?: "",
                salaryRange = entity.job.salary,
                postedDate = entity.job.postedDate,
                description = entity.job.description ?: "",
                url = "", // JobEntity is missing URL field, using empty as placeholder
                sourceProvider = "DATABASE",
                employmentType = try {
                    EmploymentType.valueOf(entity.job.type ?: "FULL_TIME")
                } catch (e: Exception) {
                    EmploymentType.FULL_TIME
                }
            )
        }
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
                        industry = null
                    )
                )
            }

            jobDao.insertJob(
                JobEntity(
                    companyId = companyId,
                    title = job.title,
                    location = job.location,
                    type = job.employmentType.name,
                    salary = job.salaryRange,
                    description = job.description,
                    postedDate = job.postedDate
                )
            )
        }
    }

    override suspend fun clear() {
        jobDao.deleteAllJobs()
    }
}
