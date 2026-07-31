package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bangersoul.aivance.core.common.security.EncryptedString
import com.bangersoul.aivance.core.database.converter.EncryptedTypeConverters

@Entity(tableName = "resumes")
data class ResumeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val primaryVersionId: Long? = null,
    val fileName: String? = null,
    val originalFileUri: String? = null,
    @TypeConverters(EncryptedTypeConverters::class)
    val rawText: EncryptedString? = null,
    val dateCreated: Long,
    val lastModified: Long
)
