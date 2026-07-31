package com.bangersoul.aivance.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "jobs",
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
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "companyId")
    val companyId: Long,
    val title: String,
    val location: String?,
    val type: String?, // Normalized EmploymentType
    val remoteType: String?, // Normalized RemoteType
    val experienceLevel: String?, // Normalized ExperienceLevel
    val salaryMin: Double?,
    val salaryMax: Double?,
    val currency: String?,
    val description: String?,
    val descriptionHtml: String?,
    val url: String,
    val sourceProviderId: String,
    val postedDate: Long,
    val expirationDate: Long? = null
)
