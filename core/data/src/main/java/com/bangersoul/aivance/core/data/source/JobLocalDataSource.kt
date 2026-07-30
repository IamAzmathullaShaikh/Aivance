package com.bangersoul.aivance.core.data.source

import com.bangersoul.aivance.core.common.model.Company
import com.bangersoul.aivance.core.common.model.JobApplication
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.SearchFilter
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.database.dao.CompanyDao
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.dao.SearchDao
import com.bangersoul.aivance.core.database.dao.TrackerDao
import com.bangersoul.aivance.core.database.model.CompanyEntity
import com.bangersoul.aivance.core.database.model.JobApplicationEntity
import com.bangersoul.aivance.core.database.model.JobEntity
import com.bangersoul.aivance.core.database.model.SavedSearchEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

interface JobLocalDataSource {
    fun getJobs(): Flow<List<JobListing>>
    fun getJobsPaginated(limit: Int, offset: Int): Flow<List<JobListing>>
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

    override fun getJobsPaginated(limit: Int, offset: Int): Flow<List<JobListing>> {
        return jobDao.getJobsPaginated(limit, offset).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getJobById(id: Long): JobListing? {
        // JobDao.getJobById returns JobEntity, but we need JobListing which needs Company info.
        // I might need a getJobWithDetailsById in JobDao.
        // For now, I'll return what I can or just map it if possible.
        // Actually, JobListing needs company name.
        val jobEntity = jobDao.getJobById(id) ?: return null
        val companyEntity = companyDao.getCompanyById(jobEntity.companyId)
        
        return JobListing(
            id = jobEntity.id.toString(),
            title = jobEntity.title,
            company = companyEntity?.name ?: "Unknown",
            location = jobEntity.location ?: "",
            salaryRange = jobEntity.salary,
            description = jobEntity.description ?: "",
            url = "",
            sourceProvider = "LOCAL",
            postedDate = jobEntity.postedDate
        )
    }

    override suspend fun saveJob(job: JobListing, companyId: Long): Long {
        val entity = JobEntity(
            id = job.id.toLongOrNull() ?: 0L,
            companyId = companyId,
            title = job.title,
            location = job.location,
            type = job.jobType.name,
            salary = job.salaryRange,
            description = job.description,
            postedDate = job.postedDate
        )
        return jobDao.insertJob(entity)
    }

    override fun getCompanies(): Flow<List<Company>> {
        return companyDao.getCompanies().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveCompany(company: Company): Long {
        val entity = CompanyEntity(
            id = company.id.toLongOrNull() ?: 0L,
            name = company.name,
            logoUrl = company.logoUrl,
            website = company.websiteUrl,
            industry = company.industry
        )
        return companyDao.insertCompany(entity)
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
