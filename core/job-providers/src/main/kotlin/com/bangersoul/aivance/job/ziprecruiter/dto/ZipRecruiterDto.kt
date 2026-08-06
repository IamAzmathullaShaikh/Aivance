package com.bangersoul.aivance.job.ziprecruiter.dto

import kotlinx.serialization.Serializable

@Serializable
data class ZipRecruiterResponseDto(
    val jobs: List<ZipRecruiterJobDto>? = null,
    val success: Boolean? = true
)

@Serializable
data class ZipRecruiterJobDto(
    val id: String? = null,
    val name: String? = null,
    val hiredCompany: ZipRecruiterCompanyDto? = null,
    val location: String? = null,
    val snippet: String? = null,
    val url: String? = null,
    val salary: String? = null,
    val postedTime: String? = null
)

@Serializable
data class ZipRecruiterCompanyDto(
    val name: String? = null
)
