package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

/**
 * Fetches job details.
 */
class GetJobDetailsUseCase @Inject constructor(
    private val jobRepository: JobRepository
) : UseCase<String, CoreResult<JobListing>>() {

    override suspend operator fun invoke(input: String): CoreResult<JobListing> {
        return jobRepository.getJobById(input)
    }
}
