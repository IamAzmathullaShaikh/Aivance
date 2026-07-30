package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bangersoul.aivance.core.database.model.ResumeAnalysisEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AtsDao {
    @Query("SELECT * FROM resume_analyses ORDER BY date DESC")
    fun getAtsResults(): Flow<List<ResumeAnalysisEntity>>

    @Query("SELECT * FROM resume_analyses ORDER BY date DESC LIMIT 1")
    fun getLatestAtsResult(): Flow<ResumeAnalysisEntity?>

    @Query("SELECT * FROM resume_analyses WHERE id = :id")
    suspend fun getAtsResultById(id: Long): ResumeAnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAtsResult(atsResult: ResumeAnalysisEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAtsResults(results: List<ResumeAnalysisEntity>)

    @Delete
    suspend fun deleteAtsResult(atsResult: ResumeAnalysisEntity)
}
