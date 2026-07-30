package com.bangersoul.aivance.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bangersoul.aivance.core.database.model.JobApplicationEntity
import com.bangersoul.aivance.core.database.model.JobApplicationWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {
    @Transaction
    @Query("SELECT * FROM job_applications ORDER BY dateApplied DESC")
    fun getApplicationsPagingSource(): PagingSource<Int, JobApplicationWithDetails>

    @Transaction
    @Query("SELECT * FROM job_applications ORDER BY dateApplied DESC")
    fun getApplications(): Flow<List<JobApplicationWithDetails>>

    @Transaction
    @Query("SELECT * FROM job_applications ORDER BY dateApplied DESC LIMIT :limit OFFSET :offset")
    fun getApplicationsPaginated(limit: Int, offset: Int): Flow<List<JobApplicationWithDetails>>

    @Transaction
    @Query("SELECT * FROM job_applications WHERE id = :id")
    suspend fun getApplicationWithDetailsById(id: Long): JobApplicationWithDetails?

    @Query("SELECT * FROM job_applications WHERE id = :id")
    suspend fun getApplicationById(id: Long): JobApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: JobApplicationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplications(applications: List<JobApplicationEntity>)

    @Update
    suspend fun updateApplication(application: JobApplicationEntity)

    @Query("UPDATE job_applications SET status = :status, lastModified = :lastModified WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, lastModified: Long)

    @Delete
    suspend fun deleteApplication(application: JobApplicationEntity)
}
