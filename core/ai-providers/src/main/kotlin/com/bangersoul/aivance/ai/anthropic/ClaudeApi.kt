package com.bangersoul.aivance.ai.anthropic

import com.bangersoul.aivance.ai.anthropic.models.ClaudeMessageRequest
import com.bangersoul.aivance.ai.anthropic.models.ClaudeMessageResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Streaming

interface ClaudeApi {
    @Headers("Content-Type: application/json")
    @POST("v1/messages")
    suspend fun createMessage(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") anthropicVersion: String,
        @Body request: ClaudeMessageRequest
    ): Response<ClaudeMessageResponse>

    @Headers("Content-Type: application/json")
    @POST("v1/messages")
    @Streaming
    suspend fun createMessageStream(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") anthropicVersion: String,
        @Body request: ClaudeMessageRequest
    ): Response<ResponseBody>
}
