package com.bangersoul.aivance.job.jobicy.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JobicyJobDto(
    val id: Long? = null,
    val url: String? = null,
    @SerialName("jobSlug") val jobSlug: String? = null,
    @SerialName("jobTitle") val jobTitle: String? = null,
    @SerialName("companyName") val companyName: String? = null,
    @SerialName("companyLogo") val companyLogo: String? = null,
    @SerialName("jobIndustry") val jobIndustry: List<String>? = null,
    @SerialName("jobType") val jobType: List<String>? = null,
    @SerialName("jobGeo") val jobGeo: String? = null,
    @SerialName("jobLevel") val jobLevel: String? = null,
    @SerialName("jobExcerpt") val jobExcerpt: String? = null,
    @SerialName("jobDescription") val jobDescription: String? = null,
    @SerialName("pubDate") val pubDate: String? = null,
    @SerialName("annualSalaryMin") val annualSalaryMin: String? = null,
    @SerialName("annualSalaryMax") val annualSalaryMax: String? = null,
    @SerialName("salaryCurrency") val salaryCurrency: String? = null
)

@Serializable
data class JobicyResponseDto(
    @SerialName("apiVersion") val apiVersion: String? = null,
    @SerialName("jobCount") val jobCount: Int = 0,
    val jobs: List<JobicyJobDto> = emptyList()
)
