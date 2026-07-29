package com.bangersoul.aivance.feature.tracker.domain

import kotlinx.coroutines.flow.Flow

interface JobTrackerRepository {
    fun getApplications(): Flow<List<JobApplication>>
    suspend fun getApplicationById(id: Long): JobApplication?
    suspend fun addApplication(application: JobApplication)
    suspend fun updateApplication(application: JobApplication)
    suspend fun deleteApplication(application: JobApplication)
}
