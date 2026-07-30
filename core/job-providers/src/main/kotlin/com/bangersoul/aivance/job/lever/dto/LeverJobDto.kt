package com.bangersoul.aivance.job.lever.dto

import kotlinx.serialization.Serializable

@Serializable
data class LeverCategoriesDto(
    val location: String? = null,
    val commitment: String? = null,
    val team: String? = null
)

@Serializable
data class LeverJobDto(
    val id: String? = null,
    val text: String? = null, // This is the title in Lever API
    val categories: LeverCategoriesDto? = null,
    val applyUrl: String? = null,
    val createdAt: Long? = null,
    val description: String? = null,
    val descriptionHtml: String? = null
)
