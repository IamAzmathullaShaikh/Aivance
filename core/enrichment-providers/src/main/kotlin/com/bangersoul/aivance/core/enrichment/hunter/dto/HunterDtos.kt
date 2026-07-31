package com.bangersoul.aivance.core.enrichment.hunter.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HunterDomainSearchResponse(
    val data: HunterDomainSearchData? = null,
    val errors: List<HunterErrorDto>? = null
)

@Serializable
data class HunterDomainSearchData(
    val domain: String? = null,
    val organization: String? = null,
    val pattern: String? = null,
    val emails: List<HunterEmailDto> = emptyList()
)

@Serializable
data class HunterEmailDto(
    val value: String? = null,
    val type: String? = null,
    val confidence: Int = 0,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    val position: String? = null,
    val linkedin: String? = null,
    @SerialName("phone_number") val phoneNumber: String? = null
)

@Serializable
data class HunterEmailVerifierResponse(
    val data: HunterEmailVerifierData? = null,
    val errors: List<HunterErrorDto>? = null
)

@Serializable
data class HunterEmailVerifierData(
    val status: String? = null,
    val result: String? = null,
    val score: Int = 0,
    val regexp: Boolean? = null,
    val disposable: Boolean? = null
)

@Serializable
data class HunterErrorDto(
    val details: String? = null,
    val code: String? = null
)
