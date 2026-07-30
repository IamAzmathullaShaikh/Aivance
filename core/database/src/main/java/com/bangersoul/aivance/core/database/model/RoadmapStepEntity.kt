package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "roadmap_steps",
    foreignKeys = [
        ForeignKey(
            entity = RoadmapEntity::class,
            parentColumns = ["id"],
            childColumns = ["roadmapId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["roadmapId"])]
)
data class RoadmapStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roadmapId: Long,
    val title: String,
    val description: String,
    val stepOrder: Int,
    val isCompleted: Boolean
)
