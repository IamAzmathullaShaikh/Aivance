package com.bangersoul.aivance.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class RoadmapWithSteps(
    @Embedded val roadmap: RoadmapEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "roadmapId"
    )
    val steps: List<RoadmapStepEntity>
)
