package com.bangersoul.aivance.core.enrichment.hunter

import com.bangersoul.aivance.core.enrichment.hunter.dto.HunterDomainSearchResponse
import com.bangersoul.aivance.core.enrichment.hunter.dto.HunterEmailVerifierResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HunterApi {
    @GET("v2/domain-search")
    suspend fun domainSearch(
        @Query("domain") domain: String,
        @Query("api_key") apiKey: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<HunterDomainSearchResponse>

    @GET("v2/email-verifier")
    suspend fun verifyEmail(
        @Query("email") email: String,
        @Query("api_key") apiKey: String
    ): Response<HunterEmailVerifierResponse>
}
