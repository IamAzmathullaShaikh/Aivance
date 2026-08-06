package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "roadmaps",
    indices = [Index(value = ["targetRole"])]
)
data class RoadmapEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetRole: String,
    val currentLevel: String,
    val description: String,
    val dateCreated: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis()
)
