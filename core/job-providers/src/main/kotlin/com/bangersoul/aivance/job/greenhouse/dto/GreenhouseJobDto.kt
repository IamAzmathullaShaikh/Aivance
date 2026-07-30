package com.bangersoul.aivance.job.greenhouse.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GreenhouseLocationDto(
    val name: String? = null
)

@Serializable
data class GreenhouseJobDto(
    val id: Long? = null,
    val title: String? = null,
    val location: GreenhouseLocationDto? = null,
    @SerialName("absolute_url") val absoluteUrl: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("content") val content: String? = null
)

@Serializable
data class GreenhouseResponseDto(
    val jobs: List<GreenhouseJobDto> = emptyList()
)
