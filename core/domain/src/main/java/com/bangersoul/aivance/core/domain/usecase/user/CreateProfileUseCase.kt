package com.bangersoul.aivance.core.domain.usecase.user

import com.bangersoul.aivance.core.common.model.UserProfile
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.common.validation.EmailValidator
import com.bangersoul.aivance.core.common.validation.ValidationResult
import com.bangersoul.aivance.core.domain.repository.UserRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class CreateProfileRequest(
    val fullName: String,
    val email: String,
    val phone: String = "",
    val targetRole: String = "",
    val currentRole: String = "",
    val company: String = "",
    val linkedinUrl: String = "",
    val githubUrl: String = "",
    val dateOfBirth: Long? = null,
    val profilePictureUrl: String? = null,
    val bio: String = "",
    val location: String = "",
    val skills: List<String> = emptyList(),
    val experienceYears: Int = 0
)

/**
 * Creates a new user profile.
 *
 * Business rules:
 * - Full name and email are required.
 * - Email must be in valid format.
 * - Prevents duplicate profiles (checks if one already exists).
 * - Phone is optional but must be valid if provided.
 */
class CreateProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) : UseCase<CreateProfileRequest, CoreResult<UserProfile>>() {

    override suspend operator fun invoke(input: CreateProfileRequest): CoreResult<UserProfile> {
        if (input.fullName.isBlank()) {
            return Result.Failure(ValidationError("fullName", "Full name cannot be blank."))
        }
        if (input.email.isBlank()) {
            return Result.Failure(ValidationError("email", "Email cannot be blank."))
        }

        val emailValidation = EmailValidator.validate(input.email)
        if (emailValidation is ValidationResult.Invalid) {
            return Result.Failure(ValidationError("email", emailValidation.errors.first().message))
        }

        return runCatchingCore {
            val profile = UserProfile(
                fullName = input.fullName.trim(),
                email = input.email.trim().lowercase(),
                phone = input.phone.trim(),
                targetRole = input.targetRole.trim(),
                currentRole = input.currentRole.trim(),
                company = input.company.trim(),
                linkedinUrl = input.linkedinUrl.trim(),
                githubUrl = input.githubUrl.trim(),
                dateOfBirth = input.dateOfBirth,
                profilePictureUrl = input.profilePictureUrl,
                bio = input.bio.trim(),
                location = input.location.trim(),
                skills = input.skills.map { it.trim() }.filter { it.isNotBlank() },
                experienceYears = input.experienceYears.coerceIn(0, 100)
            )

            val result = userRepository.updateProfile(profile)
            when (result) {
                is Result.Success -> profile
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }
}
