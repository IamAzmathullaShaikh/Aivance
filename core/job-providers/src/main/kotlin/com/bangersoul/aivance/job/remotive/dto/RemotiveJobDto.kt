package com.bangersoul.aivance.job.remotive.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemotiveJobDto(
    val id: Long? = null,
    val url: String? = null,
    val title: String? = null,
    @SerialName("company_name") val companyName: String? = null,
    @SerialName("company_logo") val companyLogo: String? = null,
    val category: String? = null,
    val tags: List<String>? = null,
    @SerialName("job_type") val jobType: String? = null,
    @SerialName("publication_date") val publicationDate: String? = null,
    @SerialName("candidate_required_location") val candidateLocation: String? = null,
    val salary: String? = null,
    val description: String? = null
)

@Serializable
data class RemotiveResponseDto(
    @SerialName("job-count") val jobCount: Int? = null,
    val jobs: List<RemotiveJobDto> = emptyList()
)
