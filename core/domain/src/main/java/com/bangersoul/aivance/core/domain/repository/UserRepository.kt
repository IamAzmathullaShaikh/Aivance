package com.bangersoul.aivance.core.domain.repository

import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.CoreResult
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getProfile(): Flow<CoreResult<UserProfile>>
    suspend fun updateProfile(profile: UserProfile): CoreResult<Unit>
}
