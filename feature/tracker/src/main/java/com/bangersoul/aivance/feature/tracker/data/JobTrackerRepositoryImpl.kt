package com.bangersoul.aivance.feature.tracker.data

import com.bangersoul.aivance.core.database.dao.CompanyDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.dao.TrackerDao
import com.bangersoul.aivance.core.database.model.CompanyEntity
import com.bangersoul.aivance.core.database.model.JobApplicationEntity
import com.bangersoul.aivance.core.database.model.JobApplicationWithDetails
import com.bangersoul.aivance.core.database.model.JobEntity
import com.bangersoul.aivance.feature.tracker.domain.ApplicationStatus
import com.bangersoul.aivance.feature.tracker.domain.JobApplication
import com.bangersoul.aivance.feature.tracker.domain.JobTrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class JobTrackerRepositoryImpl @Inject constructor(
    private val trackerDao: TrackerDao,
    private val jobDao: JobDao,
    private val companyDao: CompanyDao
) : JobTrackerRepository {

    override fun getApplications(): Flow<List<JobApplication>> {
        return trackerDao.getApplications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getApplicationById(id: Long): JobApplication? {
        return trackerDao.getApplicationWithDetailsById(id)?.toDomain()
    }

    override suspend fun addApplication(application: JobApplication) {
        val jobId = ensureJobId(application.company, application.role)
        trackerDao.insertApplication(application.toEntity(jobId))
    }

    override suspend fun updateApplication(application: JobApplication) {
        val jobId = ensureJobId(application.company, application.role)
        trackerDao.updateApplication(application.toEntity(jobId))
    }

    override suspend fun deleteApplication(application: JobApplication) {
        val jobId = ensureJobId(application.company, application.role)
        trackerDao.deleteApplication(application.toEntity(jobId))
    }

    private suspend fun ensureJobId(companyName: String, role: String): Long {
        val companyId = companyDao.getCompanyByName(companyName)?.id
            ?: companyDao.insertCompany(CompanyEntity(name = companyName, logoUrl = null, website = null, industry = null))
        
        return jobDao.getJobByCompanyAndTitle(companyId, role)?.id
            ?: jobDao.insertJob(JobEntity(companyId = companyId, title = role, location = null, type = null, salary = null, description = null, postedDate = System.currentTimeMillis()))
    }
}

private fun JobApplicationWithDetails.toDomain() = JobApplication(
    id = application.id,
    company = job.company.name,
    role = job.job.title,
    status = ApplicationStatus.valueOf(application.status),
    dateApplied = Instant.ofEpochMilli(application.dateApplied),
    salaryRange = application.salaryRange,
    notes = application.notes,
    lastModified = Instant.ofEpochMilli(application.lastModified)
)

private fun JobApplication.toEntity(jobId: Long) = JobApplicationEntity(
    id = id,
    jobId = jobId,
    status = status.name,
    dateApplied = dateApplied.toEpochMilli(),
    salaryRange = salaryRange,
    notes = notes,
    lastModified = lastModified.toEpochMilli()
)
