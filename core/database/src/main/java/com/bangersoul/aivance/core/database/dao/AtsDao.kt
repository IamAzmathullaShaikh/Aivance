package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bangersoul.aivance.core.database.model.AtsResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AtsDao {
    @Query("SELECT * FROM ats_results ORDER BY date DESC")
    fun getAtsResults(): Flow<List<AtsResultEntity>>

    @Query("SELECT * FROM ats_results ORDER BY date DESC LIMIT 1")
    fun getLatestAtsResult(): Flow<AtsResultEntity?>

    @Query("SELECT * FROM ats_results WHERE id = :id")
    suspend fun getAtsResultById(id: Long): AtsResultEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAtsResult(atsResult: AtsResultEntity)

    @Delete
    suspend fun deleteAtsResult(atsResult: AtsResultEntity)
}
