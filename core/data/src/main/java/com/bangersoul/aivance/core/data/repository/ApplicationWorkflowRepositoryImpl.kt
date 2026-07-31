package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.Application
import com.bangersoul.aivance.core.common.model.ApplicationStage
import com.bangersoul.aivance.core.common.model.ApplicationTask
import com.bangersoul.aivance.core.common.model.TimelineEvent
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.mapper.toDomain
import com.bangersoul.aivance.core.data.mapper.toEntity
import com.bangersoul.aivance.core.database.dao.JobDao
import com.bangersoul.aivance.core.database.dao.WorkflowDao
import com.bangersoul.aivance.core.domain.repository.ApplicationWorkflowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationWorkflowRepositoryImpl @Inject constructor(
    private val workflowDao: WorkflowDao,
    private val jobDao: JobDao
) : ApplicationWorkflowRepository {

    override fun getApplications(): Flow<CoreResult<List<Application>>> {
        return workflowDao.getAllApplications().map { entities ->
            runCatchingCore {
                entities.map { entity ->
                    val job = jobDao.getJobWithDetailsById(entity.jobId)?.toDomain()
                    entity.toDomain(job = job)
                }
            }
        }
    }

    override fun getApplicationById(id: Long): Flow<CoreResult<Application>> {
        // Implementation for single application with full context (timeline, tasks)
        return workflowDao.getAllApplications().map { list ->
            runCatchingCore {
                val entity = list.find { it.id == id } ?: throw Exception("Application not found")
                val job = jobDao.getJobWithDetailsById(entity.jobId)?.toDomain()
                val timeline = workflowDao.getTimelineForApplication(id).firstOrNull() ?: emptyList()
                val tasks = workflowDao.getTasksForApplication(id).firstOrNull() ?: emptyList()

                entity.toDomain(
                    job = job,
                    timeline = timeline.map { it.toDomain() },
                    tasks = tasks.map { it.toDomain() }
                )
            }
        }
    }

    override suspend fun saveApplication(application: Application): CoreResult<Long> = runCatchingCore {
        workflowDao.insertApplication(application.toEntity())
    }

    override suspend fun deleteApplication(id: Long): CoreResult<Unit> = runCatchingCore {
        val entity = workflowDao.getApplicationById(id) ?: throw Exception("Not found")
        workflowDao.deleteApplication(entity)
    }

    override fun getStages(): Flow<CoreResult<List<ApplicationStage>>> {
        return workflowDao.getStages().map { entities ->
            runCatchingCore { entities.map { it.toDomain() } }
        }
    }

    override suspend fun addTimelineEvent(event: TimelineEvent): CoreResult<Long> = runCatchingCore {
        workflowDao.insertTimelineEvent(event.toEntity())
    }

    override suspend fun addTask(task: ApplicationTask): CoreResult<Long> = runCatchingCore {
        workflowDao.insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: ApplicationTask): CoreResult<Unit> = runCatchingCore {
        workflowDao.updateTask(task.toEntity())
    }
}
