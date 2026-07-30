package com.bangersoul.aivance.sdk.api

import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.sdk.core.BaseProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata

/**
 * Interface for job search providers.
 * Inherits from [BaseProvider] to manage metadata and lifecycle.
 */
abstract class JobProvider(
    metadata: ProviderMetadata,
    capabilities: Set<ProviderCapability>
) : BaseProvider(metadata, capabilities) {

    /**
     * Searches for jobs based on a filter and sort order.
     * @param filter The search keywords and advanced filters.
     * @param sortOrder The order in which results should be returned.
     * @param page The page number for pagination.
     * @return Result containing a list of job listings or an error.
     */
    abstract suspend fun searchJobs(
        filter: JobSearchFilter,
        sortOrder: JobSortOrder,
        page: Int
    ): Result<List<JobListing>>

    /**
     * Fetches detailed information for a specific job.
     * @param jobId The unique identifier of the job.
     * @return Result containing the full job listing or an error.
     */
    abstract suspend fun getJobDetails(jobId: String): Result<JobListing>
}
