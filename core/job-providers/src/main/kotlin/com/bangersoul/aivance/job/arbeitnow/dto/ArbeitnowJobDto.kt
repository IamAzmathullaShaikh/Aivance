package com.bangersoul.aivance.job.arbeitnow.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ArbeitnowJobDto(
    val slug: String? = null,
    @SerialName("company_name") val companyName: String? = null,
    val title: String? = null,
    val description: String? = null,
    val remote: Boolean? = null,
    val url: String? = null,
    /** Tolerant of both array and object shapes in the live API. */
    val tags: JsonElement? = null,
    @SerialName("job_types") val jobTypes: List<String>? = null,
    val location: String? = null,
    @SerialName("created_at") val createdAt: Long? = null
)

@Serializable
data class ArbeitnowResponseDto(
    val data: List<ArbeitnowJobDto> = emptyList(),
    val meta: ArbeitnowMetaDto? = null
)

@Serializable
data class ArbeitnowMetaDto(
    val count: Int = 0,
    val page: Int = 1
)
