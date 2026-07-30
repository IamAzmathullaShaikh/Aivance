package com.bangersoul.aivance.core.domain.usecase.job

import androidx.paging.PagingData
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.SearchFilter
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class SearchRemoteJobsRequest(
    val query: String,
    val minSalary: Int? = null,
    val maxSalary: Int? = null
)

/**
 * Searches exclusively for remote jobs.
 *
 * Business rules:
 * - Delegates to SearchJobsUseCase with isRemote=true.
 * - Requires a non-blank query.
 */
class SearchRemoteJobsUseCase @Inject constructor(
    private val jobRepository: JobRepository
) : FlowUseCase<SearchRemoteJobsRequest, PagingData<JobListing>>() {

    override fun invoke(input: SearchRemoteJobsRequest): Flow<PagingData<JobListing>> {
        if (input.query.isBlank()) {
            return kotlinx.coroutines.flow.emptyFlow()
        }

        val filter = SearchFilter(
            keywords = input.query,
            isRemote = true,
            minSalary = input.minSalary,
            maxSalary = input.maxSalary
        )

        return jobRepository.searchJobs(input.query, filter)
    }
}
