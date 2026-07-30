package com.bangersoul.aivance.core.domain.usecase.user

import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.UserRepository
import com.bangersoul.aivance.core.domain.usecase.NoInputUseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Deletes the user profile and resets to defaults.
 *
 * Business rules:
 * - Removes the existing profile from storage.
 * - Does not delete other user data (resumes, applications, etc.).
 * - After deletion, a new default profile can be created.
 */
class DeleteProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) : NoInputUseCase<CoreResult<Unit>>() {

    override suspend operator fun invoke(): CoreResult<Unit> {
        return runCatchingCore {
            val existingResult = userRepository.getProfile().firstOrNull()
            when (existingResult) {
                is Result.Success -> {
                    val emptyProfile = UserProfile(fullName = "", email = "")
                    userRepository.updateProfile(emptyProfile)
                }
                is Result.Failure -> throw Exception(existingResult.error.message)
                null -> throw Exception("No profile found to delete.")
            }
        }
    }
}
