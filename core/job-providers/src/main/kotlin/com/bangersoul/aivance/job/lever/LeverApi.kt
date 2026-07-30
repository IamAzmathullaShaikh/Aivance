package com.bangersoul.aivance.job.lever

import com.bangersoul.aivance.job.lever.dto.LeverJobDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LeverApi {
    @GET("postings/{companyId}")
    suspend fun getJobs(
        @Path("companyId") companyId: String,
        @Query("mode") mode: String = "json"
    ): Response<List<LeverJobDto>>
}
