package com.bangersoul.aivance.job.naukri.dto

import kotlinx.serialization.Serializable

@Serializable
data class NaukriResponseDto(
    val jobDetails: List<NaukriJobDto>? = null
)

@Serializable
data class NaukriJobDto(
    val jobId: String? = null,
    val title: String? = null,
    val companyName: String? = null,
    val location: String? = null,
    val salary: String? = null,
    val jobDescription: String? = null,
    val staticUrl: String? = null,
    val createdDate: Long? = null
)
