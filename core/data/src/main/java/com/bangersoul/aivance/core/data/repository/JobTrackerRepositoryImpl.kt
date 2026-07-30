package com.bangersoul.aivance.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.bangersoul.aivance.core.common.model.JobApplication
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.source.JobLocalDataSource
import com.bangersoul.aivance.core.database.dao.TrackerDao
import com.bangersoul.aivance.core.database.model.JobApplicationEntity
import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class JobTrackerRepositoryImpl @Inject constructor(
    private val localDataSource: JobLocalDataSource,
    private val trackerDao: TrackerDao
) : JobTrackerRepository {

    override fun getApplications(): Flow<PagingData<JobApplication>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { trackerDao.getApplicationsPagingSource() }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun getApplicationById(id: Long): Flow<CoreResult<JobApplication>> {
        return localDataSource.getApplications().map { apps ->
            runCatchingCore { apps.find { it.id == id } ?: throw Exception("Application not found") }
        }
    }

    override suspend fun insertApplication(application: JobApplication): CoreResult<Long> = runCatchingCore {
        localDataSource.saveApplication(application, application.id)
    }

    override suspend fun updateApplication(application: JobApplication): CoreResult<Unit> = runCatchingCore {
        val existing = trackerDao.getApplicationById(application.id) ?: throw Exception("Application not found")
        trackerDao.updateApplication(
            JobApplicationEntity(
                id = application.id,
                jobId = existing.jobId,
                status = application.status.name,
                dateApplied = application.dateApplied,
                salaryRange = application.salaryRange,
                notes = application.notes,
                lastModified = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteApplication(id: Long): CoreResult<Unit> = runCatchingCore {
        trackerDao.getApplicationById(id)?.let {
            trackerDao.deleteApplication(it)
        }
    }
}
