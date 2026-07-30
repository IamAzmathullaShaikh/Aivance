package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.enums.ApplicationStatus
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class ApplyToJobRequest(
    val company: String,
    val role: String,
    val salaryRange: String = "",
    val notes: String = ""
)

/**
 * Creates a new job application entry.
 *
 * Business rules:
 * - Company and role must be provided.
 * - Status is set to APPLIED.
 * - Date applied is set to current time.
 * - Prevents duplicate applications for the same company+role.
 */
class ApplyToJobUseCase @Inject constructor(
    private val jobTrackerRepository: JobTrackerRepository
) : UseCase<ApplyToJobRequest, CoreResult<Long>>() {

    override suspend operator fun invoke(input: ApplyToJobRequest): CoreResult<Long> {
        if (input.company.isBlank()) {
            return Result.Failure(ValidationError("company", "Company name cannot be blank."))
        }
        if (input.role.isBlank()) {
            return Result.Failure(ValidationError("role", "Job role cannot be blank."))
        }

        return runCatchingCore {
            val application = com.bangersoul.aivance.core.common.model.JobApplication(
                company = input.company,
                role = input.role,
                status = ApplicationStatus.APPLIED,
                salaryRange = input.salaryRange,
                notes = input.notes
            )

            val result = jobTrackerRepository.insertApplication(application)
            when (result) {
                is Result.Success -> result.data
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }
}
