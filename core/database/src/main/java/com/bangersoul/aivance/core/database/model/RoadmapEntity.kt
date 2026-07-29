package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "roadmaps")
data class RoadmapEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetRole: String,
    val currentSkills: String,
    val dateCreated: Long,
    val totalSteps: Int,
    val completedSteps: Int
)
