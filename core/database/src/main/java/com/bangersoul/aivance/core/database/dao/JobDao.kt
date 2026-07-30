package com.bangersoul.aivance.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bangersoul.aivance.core.database.model.JobEntity
import com.bangersoul.aivance.core.database.model.JobWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Transaction
    @Query("SELECT * FROM jobs ORDER BY postedDate DESC")
    fun getJobsPagingSource(): PagingSource<Int, JobWithDetails>

    @Transaction
    @Query("SELECT * FROM jobs ORDER BY postedDate DESC")
    fun getJobsWithDetails(): Flow<List<JobWithDetails>>

    @Transaction
    @Query("SELECT * FROM jobs ORDER BY postedDate DESC LIMIT :limit OFFSET :offset")
    fun getJobsPaginated(limit: Int, offset: Int): Flow<List<JobWithDetails>>

    @Query("SELECT * FROM jobs WHERE companyId = :companyId AND title = :title LIMIT 1")
    suspend fun getJobByCompanyAndTitle(companyId: Long, title: String): JobEntity?

    @Query("SELECT * FROM jobs WHERE id = :id")
    suspend fun getJobById(id: Long): JobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Query("DELETE FROM jobs")
    suspend fun deleteAllJobs()

    @Delete
    suspend fun deleteJob(job: JobEntity)
}
