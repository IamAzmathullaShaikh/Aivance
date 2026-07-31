package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analytics_snapshots")
data class AnalyticsSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val kpiJson: String, // Map of KPI keys to values
    val careerScore: Int,
    val dimensionScoresJson: String // Breakdown of career score
)

@Entity(tableName = "recommendations")
data class RecommendationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val priority: String, // HIGH, MEDIUM, LOW
    val category: String, // RESUME, NETWORKING, SKILLS, etc.
    val actionDeepLink: String?,
    val evidenceJson: String?, // Why was this recommended?
    val isDismissed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "career_goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String?,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val unit: String, // COUNT, PERCENTAGE, etc.
    val deadline: Long?,
    val isCompleted: Boolean = false,
    val type: String // SYSTEM, USER
)
