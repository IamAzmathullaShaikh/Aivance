package com.bangersoul.aivance.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bangersoul.aivance.core.database.model.AnalyticsSnapshotEntity
import com.bangersoul.aivance.core.database.model.GoalEntity
import com.bangersoul.aivance.core.database.model.RecommendationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {

    // Snapshots
    @Query("SELECT * FROM analytics_snapshots ORDER BY timestamp DESC")
    fun getSnapshots(): Flow<List<AnalyticsSnapshotEntity>>

    @Query("SELECT * FROM analytics_snapshots ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSnapshot(): AnalyticsSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: AnalyticsSnapshotEntity): Long

    // Recommendations
    @Query("SELECT * FROM recommendations WHERE isDismissed = 0 ORDER BY priority DESC, timestamp DESC")
    fun getActiveRecommendations(): Flow<List<RecommendationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendation(recommendation: RecommendationEntity): Long

    @Query("UPDATE recommendations SET isDismissed = 1 WHERE id = :id")
    suspend fun dismissRecommendation(id: Long)

    // Goals
    @Query("SELECT * FROM career_goals ORDER BY deadline ASC")
    fun getGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Query("UPDATE career_goals SET currentValue = :progress WHERE id = :id")
    suspend fun updateGoalProgress(id: Long, progress: Double)
}
