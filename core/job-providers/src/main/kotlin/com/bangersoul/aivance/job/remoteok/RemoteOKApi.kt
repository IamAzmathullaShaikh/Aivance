package com.bangersoul.aivance.job.remoteok

import com.bangersoul.aivance.job.remoteok.dto.RemoteOKJobDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RemoteOKApi {
    @GET("api")
    suspend fun getJobs(
        @Query("tag") tag: String? = null,
        @Query("location") location: String? = null
    ): Response<List<RemoteOKJobDto>>
}
