package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cover_letters")
data class CoverLetterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val company: String,
    val role: String,
    val content: String,
    val dateCreated: Long,
    val tone: String
)
