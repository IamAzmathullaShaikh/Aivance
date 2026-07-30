package com.bangersoul.aivance.job.remotive

import com.bangersoul.aivance.job.remotive.dto.RemotiveResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RemotiveApi {
    @GET("api/remote-jobs")
    suspend fun getJobs(
        @Query("search") query: String? = null,
        @Query("category") category: String? = null,
        @Query("limit") limit: Int? = null
    ): Response<RemotiveResponseDto>
}
