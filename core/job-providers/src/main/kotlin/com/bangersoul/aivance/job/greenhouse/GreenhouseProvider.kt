package com.bangersoul.aivance.job.greenhouse

import com.bangersoul.aivance.core.common.enums.EmploymentType
import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.enums.RemoteType
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.ProviderError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.job.base.RestJobProvider
import com.bangersoul.aivance.job.cache.JobCache
import com.bangersoul.aivance.job.greenhouse.dto.GreenhouseJobDto
import com.bangersoul.aivance.sdk.core.ConfigField
import com.bangersoul.aivance.sdk.core.FieldType
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.text.SimpleDateFormat
import java.util.Locale

class GreenhouseProvider(
    private val boardToken: String,
    jobCache: JobCache,
    okHttpClient: OkHttpClient,
    retrofit: Retrofit
) : RestJobProvider(
    metadata = ProviderMetadata(
        id = "greenhouse",
        name = "Greenhouse",
        type = ProviderType.JOB,
        version = "1.0.0",
        description = "Job listings from Greenhouse ATS.",
        icon = "https://www.greenhouse.io/favicon.ico",
        author = "BangerSoul",
        configFields = listOf(
            ConfigField(
                key = "boardToken",
                label = "Greenhouse Board Token",
                isRequired = true
            )
        )
    ),
    capabilities = setOf(ProviderCapability.JobSearch),
    jobCache = jobCache,
    baseOkHttpClient = okHttpClient,
    baseRetrofit = retrofit
) {
    override val baseUrl: String = "https://boards-api.greenhouse.io/v1/"

    private val api: GreenhouseApi by lazy { retrofit.create(GreenhouseApi::class.java) }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)

    override suspend fun executeSearch(
        filter: JobSearchFilter,
        sortOrder: JobSortOrder,
        page: Int
    ): List<JobListing> {
        val response = api.getJobs(boardToken)
        if (response.isSuccessful) {
            return response.body()?.jobs?.filter {
                it.title?.contains(filter.query, ignoreCase = true) == true &&
                (filter.location.isBlank() || it.location?.name?.contains(filter.location, ignoreCase = true) == true)
            }?.map { mapToJobListing(it) } ?: emptyList()
        } else {
            throw Exception("Greenhouse API failed: ${response.code()}")
        }
    }

    override suspend fun getJobDetails(jobId: String): Result<JobListing> {
        return Result.Failure(ProviderError(metadata.id, message = "Direct job detail fetch not supported"))
    }

    private fun mapToJobListing(dto: GreenhouseJobDto): JobListing {
        return JobListing(
            id = dto.id?.toString() ?: "",
            title = dto.title ?: "No Title",
            company = boardToken.replaceFirstChar { it.uppercase() },
            location = dto.location?.name ?: "Unknown",
            employmentType = EmploymentType.FULL_TIME,
            remoteType = if (dto.location?.name?.contains("Remote", ignoreCase = true) == true) RemoteType.REMOTE else RemoteType.ON_SITE,
            isRemote = dto.location?.name?.contains("Remote", ignoreCase = true) == true,
            description = dto.content ?: "",
            url = dto.absoluteUrl ?: "",
            sourceProvider = metadata.id,
            postedDate = try {
                dto.updatedAt?.let { dateFormat.parse(it)?.time } ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        )
    }
}
