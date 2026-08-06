package com.bangersoul.aivance.job.bayt.dto

import kotlinx.serialization.Serializable

@Serializable
data class BaytResponseDto(
    val results: List<BaytJobDto>? = null
)

@Serializable
data class BaytJobDto(
    val id: String? = null,
    val title: String? = null,
    val company: String? = null,
    val location: String? = null,
    val salary: String? = null,
    val description: String? = null,
    val link: String? = null,
    val datePosted: String? = null
)
