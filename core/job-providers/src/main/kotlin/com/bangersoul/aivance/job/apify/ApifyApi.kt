package com.bangersoul.aivance.job.apify

import com.bangersoul.aivance.job.apify.dto.ApifyActorRunResponse
import com.bangersoul.aivance.job.apify.dto.ApifyDatasetItem
import kotlinx.serialization.json.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApifyApi {
    @POST("acts/{actorId}/runs")
    suspend fun runActor(
        @Path("actorId") actorId: String,
        @Query("token") token: String,
        @Body input: JsonObject
    ): Response<ApifyActorRunResponse>

    @GET("actor-runs/{runId}")
    suspend fun getActorRun(
        @Path("runId") runId: String,
        @Query("token") token: String
    ): Response<ApifyActorRunResponse>

    @GET("datasets/{datasetId}/items")
    suspend fun getDatasetItems(
        @Path("datasetId") datasetId: String,
        @Query("token") token: String,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): Response<List<ApifyDatasetItem>>
}
