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
    val currentRole: String?,
    val skills: List<String>,
    val targetRole: String?,
    val bio: String?,
    val profilePictureUrl: String?
)
