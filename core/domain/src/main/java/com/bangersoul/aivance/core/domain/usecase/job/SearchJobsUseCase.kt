package com.bangersoul.aivance.core.domain.usecase.job

import androidx.paging.PagingData
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.SearchFilter
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.JobRepository
import com.bangersoul.aivance.core.domain.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class SearchJobsRequest(
    val query: String,
    val location: String = "",
    val isRemote: Boolean = false,
    val minSalary: Int? = null,
    val maxSalary: Int? = null,
    val jobType: String? = null,
    val sourceProvider: String? = null
)

/**
 * Searches for jobs using configured job providers.
 *
 * Business rules:
 * - Search query must not be blank.
 * - Supports location, salary, and remote filters.
 * - Results are streamed via PagingData for efficient display.
 * - Empty results return an empty paging source, not an error.
 */
class SearchJobsUseCase @Inject constructor(
    private val jobRepository: JobRepository
) : FlowUseCase<SearchJobsRequest, PagingData<JobListing>>() {

    override fun invoke(input: SearchJobsRequest): Flow<PagingData<JobListing>> {
        if (input.query.isBlank()) {
            return kotlinx.coroutines.flow.emptyFlow()
        }

        val filter = SearchFilter(
            keywords = input.query,
            location = input.location,
            isRemote = input.isRemote,
            minSalary = input.minSalary,
            maxSalary = input.maxSalary,
            sourceProvider = input.sourceProvider
        )

        return jobRepository.searchJobs(input.query, filter)
    }
}
