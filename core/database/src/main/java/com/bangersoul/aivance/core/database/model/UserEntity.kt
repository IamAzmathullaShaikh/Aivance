package com.bangersoul.aivance.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Identity row for the v2 authentication flow.
 *
 * [id] is the identity-provider subject (Google UID when wired to Firebase),
 * but the flow is provider-agnostic: any non-empty unique id creates a session.
 * Passwords are deliberately absent — email/phone are profile data only.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val googleId: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String? = null,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
