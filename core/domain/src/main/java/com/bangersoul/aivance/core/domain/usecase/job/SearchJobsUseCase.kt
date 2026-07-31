package com.bangersoul.aivance.core.domain.usecase.job

import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class SearchJobsRequest(
    val filter: JobSearchFilter = JobSearchFilter(),
    val sortOrder: JobSortOrder = JobSortOrder.DATE_DESC
)

/**
 * Orchestrates multi-provider job discovery.
 */
class SearchJobsUseCase @Inject constructor(
    private val jobRepository: JobRepository
) : UseCase<SearchJobsRequest, CoreResult<List<JobListing>>>() {

    override suspend operator fun invoke(input: SearchJobsRequest): CoreResult<List<JobListing>> {
        return jobRepository.searchJobs(input.filter, input.sortOrder)
    }
}
