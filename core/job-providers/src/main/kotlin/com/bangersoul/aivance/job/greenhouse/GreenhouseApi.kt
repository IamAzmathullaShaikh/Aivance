package com.bangersoul.aivance.job.greenhouse

import com.bangersoul.aivance.job.greenhouse.dto.GreenhouseResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GreenhouseApi {
    @GET("boards/{boardToken}/jobs")
    suspend fun getJobs(
        @Path("boardToken") boardToken: String,
        @Query("content") includeContent: Boolean = true
    ): Response<GreenhouseResponseDto>
}
