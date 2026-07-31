package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_profiles",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserProfileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val currentRole: String? = null,
    val skills: List<String> = emptyList(),
    val targetRole: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val experienceYears: Int = 0,
    val profilePictureUrl: String? = null,
    val createdDate: Long = System.currentTimeMillis()
)
