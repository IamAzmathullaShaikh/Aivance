package com.bangersoul.aivance.job.lever

import com.bangersoul.aivance.core.common.enums.EmploymentType
import com.bangersoul.aivance.core.common.enums.JobSortOrder
import com.bangersoul.aivance.core.common.enums.RemoteType
import com.bangersoul.aivance.core.common.model.JobListing
import com.bangersoul.aivance.core.common.model.JobSearchFilter
import com.bangersoul.aivance.core.common.result.ProviderError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.job.base.RestJobProvider
import com.bangersoul.aivance.job.cache.JobCache
import com.bangersoul.aivance.job.lever.dto.LeverJobDto
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class LeverProvider(
    private val companyId: String,
    jobCache: JobCache,
    okHttpClient: OkHttpClient,
    retrofit: Retrofit
) : RestJobProvider(
    metadata = ProviderMetadata(
        id = "lever",
        name = "Lever",
        version = "1.0.0",
        description = "Job listings from Lever ATS.",
        icon = "https://www.lever.co/favicon.ico",
        author = "BangerSoul"
    ),
    capabilities = setOf(ProviderCapability.JobSearch),
    jobCache = jobCache,
    baseOkHttpClient = okHttpClient,
    baseRetrofit = retrofit
) {
    override val baseUrl: String = "https://api.lever.co/v0/"

    private val api: LeverApi by lazy { retrofit.create(LeverApi::class.java) }

    override suspend fun executeSearch(
        filter: JobSearchFilter,
        sortOrder: JobSortOrder,
        page: Int
    ): List<JobListing> {
        val response = api.getJobs(companyId)
        if (response.isSuccessful) {
            return response.body()?.filter {
                it.text?.contains(filter.query, ignoreCase = true) == true &&
                (filter.location.isBlank() || it.categories?.location?.contains(filter.location, ignoreCase = true) == true)
            }?.map { mapToJobListing(it) } ?: emptyList()
        } else {
            throw Exception("Lever API failed: ${response.code()}")
        }
    }

    override suspend fun getJobDetails(jobId: String): Result<JobListing> {
        return Result.Failure(ProviderError(metadata.id, message = "Direct job detail fetch not supported"))
    }

    private fun mapToJobListing(dto: LeverJobDto): JobListing {
        return JobListing(
            id = dto.id ?: "",
            title = dto.text ?: "No Title",
            company = companyId.replaceFirstChar { it.uppercase() },
            location = dto.categories?.location ?: "Unknown",
            employmentType = parseEmploymentType(dto.categories?.commitment),
            remoteType = if (dto.categories?.location?.contains("Remote", ignoreCase = true) == true) RemoteType.REMOTE else RemoteType.ON_SITE,
            isRemote = dto.categories?.location?.contains("Remote", ignoreCase = true) == true,
            description = dto.description ?: "",
            url = dto.applyUrl ?: "",
            sourceProvider = metadata.id,
            postedDate = dto.createdAt ?: System.currentTimeMillis()
        )
    }

    private fun parseEmploymentType(commitment: String?): EmploymentType {
        return when (commitment?.lowercase()) {
            "full-time", "full time" -> EmploymentType.FULL_TIME
            "part-time", "part time" -> EmploymentType.PART_TIME
            "contract" -> EmploymentType.CONTRACT
            "internship" -> EmploymentType.INTERNSHIP
            else -> EmploymentType.FULL_TIME
        }
    }
}
