package com.bangersoul.aivance.job.arbeitnow

import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.enums.RemoteType
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.ProviderError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.job.base.RestJobProvider
import com.bangersoul.aivance.job.cache.JobCache
import com.bangersoul.aivance.job.mapper.JobMapper
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Free job board focused on Germany and the EU.
 * No API key required - works out of the box.
 */
class ArbeitnowProvider(
    jobCache: JobCache,
    okHttpClient: OkHttpClient,
    retrofit: Retrofit
) : RestJobProvider(
    metadata = ProviderMetadata(
        id = "arbeitnow",
        name = "Arbeitnow",
        type = ProviderType.JOB,
        version = "1.0.0",
        description = "Free jobs in Germany & the EU (no API key).",
        icon = "https://www.arbeitnow.com/favicon.ico",
        author = "BangerSoul"
    ),
    capabilities = setOf(ProviderCapability.JobSearch),
    jobCache = jobCache,
    baseOkHttpClient = okHttpClient,
    baseRetrofit = retrofit
) {
    override val baseUrl: String = "https://www.arbeitnow.com/"

    private val api: ArbeitnowApi by lazy { retrofit.create(ArbeitnowApi::class.java) }

    override suspend fun executeSearch(
        filter: JobSearchFilter,
        sortOrder: JobSortOrder,
        page: Int
    ): List<JobListing> {
        val response = api.getJobs(
            page = page,
            search = filter.query.ifBlank { null },
            location = filter.location.ifBlank { null },
            remote = filter.remoteType?.let { it == RemoteType.REMOTE }
        )
        if (response.isSuccessful) {
            return response.body()?.data?.map { JobMapper.mapToJobListing(it, metadata.id) } ?: emptyList()
        } else {
            throw Exception("Arbeitnow API failed: ${response.code()}")
        }
    }

    override suspend fun getJobDetails(jobId: String): Result<JobListing> {
        return Result.Failure(ProviderError(metadata.id, message = "Direct job detail fetch not supported"))
    }
}
