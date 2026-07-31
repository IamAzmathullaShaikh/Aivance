package com.bangersoul.aivance.job.adzuna

import com.bangersoul.aivance.job.adzuna.dto.AdzunaResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AdzunaApi {
    @GET("v1/api/jobs/{country}/search/{page}")
    suspend fun getJobs(
        @Path("country") country: String,
        @Path("page") page: Int,
        @Query("app_id") appId: String,
        @Query("app_key") appKey: String,
        @Query("what") what: String? = null,
        @Query("where") where: String? = null,
        @Query("results_per_page") resultsPerPage: Int = 50,
        @Query("content-type") contentType: String = "application/json"
    ): Response<AdzunaResponseDto>
}
