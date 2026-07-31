package com.bangersoul.aivance.job.usajobs.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class USAJobsResponseDto(
    @SerialName("SearchResult") val searchResult: USAJobsSearchResultDto? = null
)

@Serializable
data class USAJobsSearchResultDto(
    @SerialName("SearchResultItems") val items: List<USAJobsItemDto> = emptyList(),
    @SerialName("SearchResultCount") val count: Int = 0
)

@Serializable
data class USAJobsItemDto(
    @SerialName("MatchedObjectId") val matchedObjectId: String? = null,
    @SerialName("MatchedObjectDescriptor") val descriptor: USAJobsDescriptorDto? = null
)

@Serializable
data class USAJobsDescriptorDto(
    @SerialName("PositionID") val positionId: String? = null,
    @SerialName("PositionTitle") val positionTitle: String? = null,
    @SerialName("OrganizationName") val organizationName: String? = null,
    @SerialName("PositionLocation") val positions: List<USAJobsLocationDto>? = null,
    @SerialName("PositionOfferingType") val offeringTypes: List<USAJobsLabelDto>? = null,
    @SerialName("PositionSchedule") val schedules: List<USAJobsLabelDto>? = null,
    @SerialName("MinimumRange") val minimumRange: Double? = null,
    @SerialName("MaximumRange") val maximumRange: Double? = null,
    @SerialName("RateIntervalCode") val rateIntervalCode: String? = null,
    @SerialName("QualificationSummary") val qualificationSummary: String? = null,
    @SerialName("ApplyURI") val applyUri: String? = null,
    @SerialName("StartDate") val startDate: String? = null
)

@Serializable
data class USAJobsLocationDto(
    @SerialName("LocationName") val locationName: String? = null
)

@Serializable
data class USAJobsLabelDto(
    @SerialName("Name") val name: String? = null
)
