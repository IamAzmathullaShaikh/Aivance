package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bangersoul.aivance.core.database.model.RoadmapEntity
import com.bangersoul.aivance.core.database.model.RoadmapStepEntity
import com.bangersoul.aivance.core.database.model.RoadmapWithSteps
import kotlinx.coroutines.flow.Flow

@Dao
interface RoadmapDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoadmap(roadmap: RoadmapEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<RoadmapStepEntity>)

    @Transaction
    suspend fun insertRoadmapWithSteps(roadmap: RoadmapEntity, steps: List<RoadmapStepEntity>) {
        val roadmapId = insertRoadmap(roadmap)
        val stepsWithId = steps.map { it.copy(roadmapId = roadmapId) }
        insertSteps(stepsWithId)
    }

    @Transaction
    @Query("SELECT * FROM roadmaps ORDER BY dateCreated DESC")
    fun getRoadmapsWithSteps(): Flow<List<RoadmapWithSteps>>

    @Transaction
    @Query("SELECT * FROM roadmaps ORDER BY dateCreated DESC LIMIT 1")
    fun getCurrentRoadmapWithSteps(): Flow<RoadmapWithSteps?>

    @Transaction
    @Query("SELECT * FROM roadmaps WHERE id = :roadmapId")
    fun getRoadmapWithStepsById(roadmapId: Long): Flow<RoadmapWithSteps?>

    @Query("UPDATE roadmap_steps SET isCompleted = :isCompleted WHERE id = :stepId")
    suspend fun updateStepCompletion(stepId: Long, isCompleted: Boolean)

    @Delete
    suspend fun deleteRoadmap(roadmap: RoadmapEntity)

    @Query("DELETE FROM roadmaps WHERE id = :roadmapId")
    suspend fun deleteRoadmapById(roadmapId: Long)
}
