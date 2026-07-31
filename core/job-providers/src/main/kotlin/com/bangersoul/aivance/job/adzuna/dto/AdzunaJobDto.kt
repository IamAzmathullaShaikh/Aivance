package com.bangersoul.aivance.job.adzuna.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AdzunaJobDto(
    val id: String? = null,
    val title: String? = null,
    val company: AdzunaCompanyDto? = null,
    val location: AdzunaLocationDto? = null,
    @SerialName("salary_min") val salaryMin: Double? = null,
    @SerialName("salary_max") val salaryMax: Double? = null,
    val description: String? = null,
    @SerialName("redirect_url") val redirectUrl: String? = null,
    val created: String? = null,
    val category: AdzunaCategoryDto? = null,
    @SerialName("contract_type") val contractType: String? = null
)

@Serializable
data class AdzunaCompanyDto(
    @SerialName("display_name") val displayName: String? = null,
    val logo: String? = null
)

@Serializable
data class AdzunaLocationDto(
    @SerialName("display_name") val displayName: String? = null,
    val area: List<String>? = null
)

@Serializable
data class AdzunaCategoryDto(
    val label: String? = null
)

@Serializable
data class AdzunaResponseDto(
    val results: List<AdzunaJobDto> = emptyList(),
    val count: Int = 0
)
