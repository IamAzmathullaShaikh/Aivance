package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ats_results")
data class AtsResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Int,
    val date: Long,
    val resumeName: String,
    val missingKeywords: String,
    val feedback: String
)
