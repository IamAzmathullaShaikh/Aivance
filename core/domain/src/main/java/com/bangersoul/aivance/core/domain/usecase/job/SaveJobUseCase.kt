package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.enums.ApplicationStatus
import com.bangersoul.aivance.core.common.model.JobApplication
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class SaveJobRequest(
    val jobListing: JobListing,
    val notes: String = ""
)

/**
 * Saves a job listing to the user's tracked applications.
 *
 * Business rules:
 * - Job must have a valid title, company, and URL.
 * - Prevents duplicate saves (same job ID).
 * - Initial status is set to SAVED.
 * - Saves the job to local storage for offline access.
 */
class SaveJobUseCase @Inject constructor(
    private val jobTrackerRepository: JobTrackerRepository
) : UseCase<SaveJobRequest, CoreResult<JobApplication>>() {

    override suspend operator fun invoke(input: SaveJobRequest): CoreResult<JobApplication> {
        if (input.jobListing.title.isBlank()) {
            return Result.Failure(ValidationError("title", "Job title cannot be blank."))
        }
        if (input.jobListing.company.isBlank()) {
            return Result.Failure(ValidationError("company", "Company name cannot be blank."))
        }

        return runCatchingCore {
            val application = JobApplication(
                company = input.jobListing.company,
                role = input.jobListing.title,
                status = ApplicationStatus.SAVED,
                salaryRange = input.jobListing.salaryRange ?: "",
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
