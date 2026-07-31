package com.bangersoul.aivance.core.data.source

import com.bangersoul.aivance.core.common.model.Company
import com.bangersoul.aivance.core.common.model.JobApplication
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.SearchFilter
import com.bangersoul.aivance.core.database.dao.CompanyDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.dao.SearchDao
import com.bangersoul.aivance.core.database.dao.TrackerDao
import com.bangersoul.aivance.core.database.model.JobApplicationEntity
import com.bangersoul.aivance.core.database.model.SavedSearchEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity

interface JobLocalDataSource {
    fun getJobs(): Flow<List<JobListing>>
    suspend fun getJobById(id: Long): JobListing?
    suspend fun saveJob(job: JobListing, companyId: Long): Long

    fun getCompanies(): Flow<List<Company>>
    suspend fun saveCompany(company: Company): Long

    fun getSavedSearches(): Flow<List<SearchFilter>>
    suspend fun saveSearch(query: String, filters: Map<String, String>)

    fun getApplications(): Flow<List<JobApplication>>
    suspend fun saveApplication(application: JobApplication, jobId: Long): Long
    suspend fun updateApplicationStatus(applicationId: Long, status: String)
}

class JobLocalDataSourceImpl @Inject constructor(
    private val jobDao: JobDao,
    private val companyDao: CompanyDao,
    private val searchDao: SearchDao,
    private val trackerDao: TrackerDao
) : JobLocalDataSource {

    override fun getJobs(): Flow<List<JobListing>> {
        return jobDao.getJobsWithDetails().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getJobById(id: Long): JobListing? {
        return jobDao.getJobWithDetailsById(id)?.toDomain()
    }

    override suspend fun saveJob(job: JobListing, companyId: Long): Long {
        return jobDao.insertJob(job.toEntity(companyId))
    }

    override fun getCompanies(): Flow<List<Company>> {
        return companyDao.getCompanies().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveCompany(company: Company): Long {
        return companyDao.insertCompany(company.toEntity())
    }

    override fun getSavedSearches(): Flow<List<SearchFilter>> {
        return searchDao.getSavedSearches().map { entities ->
            entities.map { entity ->
                SearchFilter(
                    keywords = entity.query,
                    location = entity.filters["location"] ?: "",
                    isRemote = entity.filters["isRemote"]?.toBoolean() ?: false
                )
            }
        }
    }

    override suspend fun saveSearch(query: String, filters: Map<String, String>) {
        searchDao.insertSavedSearch(
            SavedSearchEntity(
                query = query,
                filters = filters,
                dateCreated = Instant.now()
            )
        )
    }

    override fun getApplications(): Flow<List<JobApplication>> {
        return trackerDao.getApplications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveApplication(application: JobApplication, jobId: Long): Long {
        val entity = JobApplicationEntity(
            id = application.id,
            jobId = jobId,
            status = application.status.name,
            dateApplied = application.dateApplied,
            salaryRange = application.salaryRange,
            notes = application.notes,
            lastModified = System.currentTimeMillis()
        )
        return trackerDao.insertApplication(entity)
    }

    override suspend fun updateApplicationStatus(applicationId: Long, status: String) {
        trackerDao.updateStatus(applicationId, status, System.currentTimeMillis())
    }
}
