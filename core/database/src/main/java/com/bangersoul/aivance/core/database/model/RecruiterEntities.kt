package com.bangersoul.aivance.core.database.model

import androidx.room.*
import com.bangersoul.aivance.core.common.security.EncryptedString
import com.bangersoul.aivance.core.database.converter.EncryptedTypeConverters

@Entity(
    tableName = "recruiters",
    foreignKeys = [
        ForeignKey(
            entity = CompanyEntity::class,
            parentColumns = ["id"],
            childColumns = ["companyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["companyId"])]
)
data class RecruiterEntity(
    @PrimaryKey
    val id: String, // UUID or Provider ID
    val companyId: Long,
    val name: String,
    val title: String?,
    val department: String?,
    val linkedinUrl: String?,
    val sourceProvider: String?,
    val status: String = "ACTIVE" // "ACTIVE", "ARCHIVED", "CONTACTED"
)

@Entity(
    tableName = "recruiter_contacts",
    foreignKeys = [
        ForeignKey(
            entity = RecruiterEntity::class,
            parentColumns = ["id"],
            childColumns = ["recruiterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recruiterId"])]
)
data class RecruiterContactEntity(
    @PrimaryKey
    val id: String,
    val recruiterId: String,
    @TypeConverters(EncryptedTypeConverters::class)
    val email: EncryptedString,
    val confidence: Int,
    val isVerified: Boolean,
    val lastUpdated: Long = System.currentTimeMillis()
)
