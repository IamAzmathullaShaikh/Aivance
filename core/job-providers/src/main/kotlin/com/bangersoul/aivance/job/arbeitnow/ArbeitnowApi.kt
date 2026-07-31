package com.bangersoul.aivance.job.arbeitnow

import com.bangersoul.aivance.job.arbeitnow.dto.ArbeitnowResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ArbeitnowApi {
    @GET("api/job-board-api")
    suspend fun getJobs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100,
        @Query("search") search: String? = null,
        @Query("location") location: String? = null,
        @Query("remote") remote: Boolean? = null
    ): Response<ArbeitnowResponseDto>
}
