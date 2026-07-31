package com.bangersoul.aivance.job.adzuna

import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.ProviderError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.job.base.RestJobProvider
import com.bangersoul.aivance.job.cache.JobCache
import com.bangersoul.aivance.job.mapper.JobMapper
import com.bangersoul.aivance.sdk.core.ConfigField
import com.bangersoul.aivance.sdk.core.FieldType
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderStatus
import com.bangersoul.aivance.sdk.core.ProviderType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Adzuna job search - free tier with a generous monthly quota.
 * Supports 16 countries: gb, us, de, fr, ca, au, in, nl, pl, at, br, nz, sg, za, mx, it.
 */
class AdzunaProvider(
    private val appId: String,
    private val appKey: String,
    private val countryCode: String = "us",
    jobCache: JobCache,
    okHttpClient: OkHttpClient,
    retrofit: Retrofit
) : RestJobProvider(
    metadata = ProviderMetadata(
        id = "adzuna",
        name = "Adzuna",
        type = ProviderType.JOB,
        version = "1.0.0",
        description = "Global jobs across 16 countries (free tier).",
        icon = "https://www.adzuna.co.uk/favicon.ico",
        author = "BangerSoul",
        configFields = listOf(
            ConfigField(
                key = "appId",
                label = "Adzuna App ID",
                isRequired = true
            ),
            ConfigField(
                key = "appKey",
                label = "Adzuna API Key",
                isRequired = true,
                isSensitive = true,
                fieldType = FieldType.PASSWORD,
                hint = "Free key from developer.adzuna.com"
            )
        )
    ),
    capabilities = setOf(ProviderCapability.JobSearch),
    jobCache = jobCache,
    baseOkHttpClient = okHttpClient,
    baseRetrofit = retrofit
) {
    override val baseUrl: String = "https://api.adzuna.com/"

    private val api: AdzunaApi by lazy { retrofit.create(AdzunaApi::class.java) }

    override suspend fun executeSearch(
        filter: JobSearchFilter,
        sortOrder: JobSortOrder,
        page: Int
    ): List<JobListing> {
        val response = api.getJobs(
            country = countryCode,
            page = page,
            appId = appId,
            appKey = appKey,
            what = filter.query.ifBlank { null },
            where = filter.location.ifBlank { null }
        )
        if (response.isSuccessful) {
            return response.body()?.results?.map { JobMapper.mapToJobListing(it, metadata.id, countryCode) } ?: emptyList()
        } else {
            throw Exception("Adzuna API failed: ${response.code()}")
        }
    }

    override suspend fun getJobDetails(jobId: String): Result<JobListing> {
        return Result.Failure(ProviderError(metadata.id, message = "Direct job detail fetch not supported"))
    }

    override suspend fun performHealthCheck() {
        if (appId.isBlank() || appKey.isBlank()) {
            throw Exception("Adzuna App ID / API Key not configured")
        }
    }

    override suspend fun onInitialize() {
        super.onInitialize()
        // Stay out of Active/Ready until the user provides real credentials,
        // so search aggregation filters this provider out.
        if (appId.isBlank() || appKey.isBlank()) {
            updateStatus(ProviderStatus.InvalidConfiguration)
        }
    }
}
