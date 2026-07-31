package com.bangersoul.aivance.job.jobicy

import com.bangersoul.aivance.job.jobicy.dto.JobicyResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface JobicyApi {
    @GET("api/v2/remote-jobs")
    suspend fun getJobs(
        @Query("count") count: Int = 100,
        @Query("geo") geo: String? = null,
        @Query("industry") industry: String? = null,
        @Query("tag") tag: String? = null
    ): Response<JobicyResponseDto>
}
