package com.bangersoul.aivance.job.glassdoor.dto

import kotlinx.serialization.Serializable

@Serializable
data class GlassdoorResponseDto(
    val jobs: List<GlassdoorJobDto>? = null
)

@Serializable
data class GlassdoorJobDto(
    val jobId: String? = null,
    val jobTitle: String? = null,
    val employerName: String? = null,
    val locationName: String? = null,
    val payPeriod: String? = null,
    val payMin: Double? = null,
    val payMax: Double? = null,
    val jobDescription: String? = null,
    val jobUrl: String? = null,
    val ageInDays: Int? = null
)
