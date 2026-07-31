package com.bangersoul.aivance.job.usajobs

import com.bangersoul.aivance.job.usajobs.dto.USAJobsResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface USAJobsApi {
    @GET("api/search")
    suspend fun search(
        @Header("Authorization-Key") apiKey: String,
        @Header("User-Agent") userAgent: String,
        @Query("Keyword") keyword: String? = null,
        @Query("LocationName") locationName: String? = null,
        @Query("RemoteIndicator") remoteIndicator: Boolean? = null,
        @Query("ResultsPerPage") resultsPerPage: Int = 50,
        @Query("Page") page: Int = 1
    ): Response<USAJobsResponseDto>
}
