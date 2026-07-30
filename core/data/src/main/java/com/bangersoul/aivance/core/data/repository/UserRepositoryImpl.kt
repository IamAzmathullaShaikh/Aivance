package com.bangersoul.aivance.core.data.repository

import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.onSuccess
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.data.cache.CacheManager
import com.bangersoul.aivance.core.data.source.UserLocalDataSource
import com.bangersoul.aivance.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val localDataSource: UserLocalDataSource,
    private val profileCache: CacheManager<String, UserProfile>
) : UserRepository {

    private val CACHE_KEY = "user_profile"

    override fun getProfile(): Flow<CoreResult<UserProfile>> {
        return localDataSource.getUserProfile().map { profile ->
            runCatchingCore {
                profile ?: throw Exception("Profile not found")
            }.also { result ->
                result.onSuccess { profileCache.put(CACHE_KEY, it) }
            }
        }
    }

    override suspend fun updateProfile(profile: UserProfile): CoreResult<Unit> = runCatchingCore {
        localDataSource.saveUserProfile(profile)
        profileCache.put(CACHE_KEY, profile)
    }
}
