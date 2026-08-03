package com.bangersoul.aivance.core.domain.usecase.user

import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Loads the user profile from local storage.
 *
 * Business rules:
 * - Returns the default or saved user profile.
 * - Returns an empty profile with default values if none exists.
 * - Does not throw an error for missing profile; returns default instead.
 */
class LoadProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<CoreResult<UserProfile>> {
        return userRepository.getProfile().map { result ->
            when (result) {
                is Result.Success -> Result.Success(result.data)
                is Result.Failure -> {
                    // Return a default profile if none exists
                    Result.Success(
                        UserProfile(
                            fullName = "",
                            email = ""
                        )
                    )
                }
            }
        }
    }
}
