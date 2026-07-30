package com.bangersoul.aivance.job.remoteok.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteOKJobDto(
    val id: String? = null,
    val slug: String? = null,
    val company: String? = null,
    @SerialName("company_logo") val companyLogo: String? = null,
    val position: String? = null,
    val tags: List<String>? = null,
    val description: String? = null,
    val location: String? = null,
    @SerialName("salary_min") val salaryMin: Double? = null,
    @SerialName("salary_max") val salaryMax: Double? = null,
    val date: String? = null,
    val url: String? = null,
    val legal: String? = null
)
