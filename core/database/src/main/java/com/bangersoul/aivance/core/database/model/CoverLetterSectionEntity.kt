package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cover_letter_sections",
    foreignKeys = [
        ForeignKey(
            entity = CoverLetterVersionEntity::class,
            parentColumns = ["id"],
            childColumns = ["versionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["versionId"])]
)
data class CoverLetterSectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val versionId: Long,
    val sectionType: String, // HEADER, GREETING, OPENING, WHY_COMPANY, WHY_ME, CLOSING, SIGNATURE
    val title: String,
    val content: String,
    val sectionOrder: Int
)
