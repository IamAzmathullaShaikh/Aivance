package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Fetches detailed information for a specific job listing.
 *
 * Business rules:
 * - Job ID must be valid and non-empty.
 * - Returns full job details including description, requirements, etc.
 * - Falls back to cached data if network is unavailable.
 */
class GetJobDetailsUseCase @Inject constructor(
    private val jobRepository: JobRepository
) : UseCase<String, CoreResult<JobListing>>() {

    override suspend operator fun invoke(jobId: String): CoreResult<JobListing> {
        if (jobId.isBlank()) {
            return Result.Failure(ValidationError("jobId", "Job ID cannot be blank."))
        }

        return runCatchingCore {
            val result = jobRepository.getJobById(jobId).firstOrNull()
            when (result) {
                is Result.Success -> result.data
                is Result.Failure -> throw Exception(result.error.message)
                null -> throw Exception("No result returned for job: $jobId")
            }
        }
    }
}
