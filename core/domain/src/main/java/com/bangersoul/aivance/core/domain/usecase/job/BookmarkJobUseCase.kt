package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.enums.ApplicationStatus
import com.bangersoul.aivance.core.common.model.JobApplication
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class BookmarkJobRequest(
    val company: String,
    val role: String,
    val salaryRange: String = "",
    val notes: String = ""
)

/**
 * Bookmarks a job for later review.
 *
 * Business rules:
 * - Company and role must be provided.
 * - Prevents duplicate bookmarks for the same company+role combination.
 * - Bookmarked jobs start with SAVED status.
 */
class BookmarkJobUseCase @Inject constructor(
    private val jobTrackerRepository: JobTrackerRepository
) : UseCase<BookmarkJobRequest, CoreResult<JobApplication>>() {

    override suspend operator fun invoke(input: BookmarkJobRequest): CoreResult<JobApplication> {
        if (input.company.isBlank()) {
            return Result.Failure(ValidationError("company", "Company name cannot be blank."))
        }
        if (input.role.isBlank()) {
            return Result.Failure(ValidationError("role", "Job role cannot be blank."))
        }

        return runCatchingCore {
            val application = JobApplication(
                company = input.company,
                role = input.role,
                status = ApplicationStatus.SAVED,
                salaryRange = input.salaryRange,
                notes = input.notes
            )

            val result = jobTrackerRepository.insertApplication(application)
            val id = when (result) {
                is Result.Success -> result.data
                is Result.Failure -> throw Exception(result.error.message)
            }

            application.copy(id = id)
        }
    }
}
