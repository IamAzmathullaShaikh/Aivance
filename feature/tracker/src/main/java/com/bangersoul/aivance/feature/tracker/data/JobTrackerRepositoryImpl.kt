package com.bangersoul.aivance.feature.tracker.data

import com.bangersoul.aivance.core.database.dao.ApplicationDao
import com.bangersoul.aivance.core.database.model.ApplicationEntity
import com.bangersoul.aivance.feature.tracker.domain.ApplicationStatus
import com.bangersoul.aivance.feature.tracker.domain.JobApplication
import com.bangersoul.aivance.feature.tracker.domain.JobTrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class JobTrackerRepositoryImpl @Inject constructor(
    private val applicationDao: ApplicationDao
) : JobTrackerRepository {

    override fun getApplications(): Flow<List<JobApplication>> {
        return applicationDao.getApplications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getApplicationById(id: Long): JobApplication? {
        return applicationDao.getApplicationById(id)?.toDomain()
    }

    override suspend fun addApplication(application: JobApplication) {
        applicationDao.insertApplication(application.toEntity())
    }

    override suspend fun updateApplication(application: JobApplication) {
        applicationDao.updateApplication(application.toEntity())
    }

    override suspend fun deleteApplication(application: JobApplication) {
        applicationDao.deleteApplication(application.toEntity())
    }
}

private fun ApplicationEntity.toDomain() = JobApplication(
    id = id,
    company = company,
    role = role,
    status = ApplicationStatus.valueOf(status),
    dateApplied = Instant.ofEpochMilli(dateApplied),
    salaryRange = salaryRange,
    notes = notes,
    lastModified = Instant.ofEpochMilli(lastModified)
)

private fun JobApplication.toEntity() = ApplicationEntity(
    id = id,
    company = company,
    role = role,
    status = status.name,
    dateApplied = dateApplied.toEpochMilli(),
    salaryRange = salaryRange,
    notes = notes,
    lastModified = lastModified.toEpochMilli()
)
