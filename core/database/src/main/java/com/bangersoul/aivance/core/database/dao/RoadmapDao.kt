package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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
    @Query("SELECT * FROM roadmaps ORDER BY dateCreated DESC LIMIT 1")
    fun getCurrentRoadmapWithSteps(): Flow<RoadmapWithSteps?>

    @Query("UPDATE roadmap_steps SET isCompleted = :isCompleted WHERE id = :stepId")
    suspend fun updateStepCompletion(stepId: Long, isCompleted: Boolean)

    @Query("UPDATE roadmaps SET completedSteps = :completedSteps WHERE id = :roadmapId")
    suspend fun updateRoadmapProgress(roadmapId: Long, completedSteps: Int)

    @Transaction
    suspend fun updateStepAndProgress(roadmapId: Long, stepId: Long, isCompleted: Boolean, completedSteps: Int) {
        updateStepCompletion(stepId, isCompleted)
        updateRoadmapProgress(roadmapId, completedSteps)
    }

    @Query("DELETE FROM roadmaps WHERE id = :roadmapId")
    suspend fun deleteRoadmap(roadmapId: Long)
}
