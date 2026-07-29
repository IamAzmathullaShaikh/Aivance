package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bangersoul.aivance.core.database.model.ApplicationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApplicationDao {
    @Query("SELECT * FROM applications ORDER BY dateApplied DESC")
    fun getApplications(): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications WHERE id = :id")
    suspend fun getApplicationById(id: Long): ApplicationEntity?

    @Query("SELECT * FROM applications WHERE status = :status AND lastModified < :threshold")
    suspend fun getApplicationsByStatusAndStale(status: String, threshold: Long): List<ApplicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: ApplicationEntity)

    @Query("UPDATE applications SET status = :status, lastModified = :lastModified WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, lastModified: Long)

    @Update
    suspend fun updateApplication(application: ApplicationEntity)

    @Delete
    suspend fun deleteApplication(application: ApplicationEntity)
}
