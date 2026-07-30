package com.bangersoul.aivance.core.domain.usecase.user

import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.common.validation.EmailValidator
import com.bangersoul.aivance.core.common.validation.ValidationResult
import com.bangersoul.aivance.core.domain.repository.UserRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class UpdateProfileRequest(
    val fullName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val targetRole: String? = null,
    val bio: String? = null,
    val location: String? = null,
    val skills: List<String>? = null,
    val experienceYears: Int? = null
)

/**
 * Updates an existing user profile with partial data.
 *
 * Business rules:
 * - Only provided fields are updated (partial update).
 * - Email format is validated if provided.
 * - Profile must already exist.
 * - Preserves existing values for fields not provided.
 */
class UpdateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<UpdateProfileRequest, CoreResult<UserProfile>>() {

    override suspend operator fun invoke(input: UpdateProfileRequest): CoreResult<UserProfile> {
        if (input.email != null) {
            if (input.email.isBlank()) {
                return Result.Failure(ValidationError("email", "Email cannot be blank."))
            }
            val emailValidation = EmailValidator.validate(input.email)
            if (emailValidation is ValidationResult.Invalid) {
                return Result.Failure(ValidationError("email", emailValidation.errors.first().message))
            }
        }

        return runCatchingCore {
            val existingResult = userRepository.getProfile().firstOrNull()
            val existing = when (existingResult) {
                is Result.Success -> existingResult.data
                is Result.Failure -> throw Exception(existingResult.error.message)
                null -> throw Exception("Profile not found.")
            }

            val updatedProfile = createUpdatedProfile(existing, input)

            val saveResult = userRepository.updateProfile(updatedProfile)
            when (saveResult) {
                is Result.Success -> updatedProfile
                is Result.Failure -> throw Exception(saveResult.error.message)
            }
        }
    }

    private fun createUpdatedProfile(existing: UserProfile, input: UpdateProfileRequest): UserProfile {
        return UserProfile(
            id = existing.id,
            fullName = input.fullName?.trim() ?: existing.fullName,
            email = input.email?.trim()?.lowercase() ?: existing.email,
            phone = input.phone?.trim() ?: existing.phone,
            targetRole = input.targetRole?.trim() ?: existing.targetRole,
            bio = input.bio?.trim() ?: existing.bio,
            location = input.location?.trim() ?: existing.location,
            skills = input.skills?.map { it.trim() }?.filter { it.isNotBlank() } ?: existing.skills,
            experienceYears = input.experienceYears?.coerceIn(0, 100) ?: existing.experienceYears,
            createdDate = existing.createdDate
        )
    }
}
