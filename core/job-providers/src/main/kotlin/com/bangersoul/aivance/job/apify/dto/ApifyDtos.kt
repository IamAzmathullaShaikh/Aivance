package com.bangersoul.aivance.job.apify.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApifyActorRunResponse(
    val data: ApifyActorRunData
)

@Serializable
data class ApifyActorRunData(
    val id: String,
    val status: String,
    val defaultDatasetId: String? = null
)

@Serializable
data class ApifyDatasetItem(
    val id: String? = null,
    val title: String? = null,
    val company: String? = null,
    val companyLogo: String? = null,
    val location: String? = null,
    val salary: String? = null,
    val description: String? = null,
    val descriptionHtml: String? = null,
    val url: String? = null,
    val postedAt: String? = null,
    val type: String? = null,
    val experienceLevel: String? = null,
    val isRemote: Boolean? = false
)
