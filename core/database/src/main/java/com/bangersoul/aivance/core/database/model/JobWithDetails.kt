package com.bangersoul.aivance.core.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class JobWithDetails(
    @Embedded val job: JobEntity,
    @Relation(
        parentColumn = "companyId",
        entityColumn = "id"
    )
    val company: CompanyEntity
)
