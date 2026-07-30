package com.bangersoul.aivance.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class JobApplicationWithDetails(
    @Embedded val application: JobApplicationEntity,
    @Relation(
        entity = JobEntity::class,
        parentColumn = "jobId",
        entityColumn = "id"
    )
    val job: JobWithDetails
)
