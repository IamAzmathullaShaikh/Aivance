package com.bangersoul.aivance.ai.openai

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

/**
 * Retrofit interface for OpenAI-compatible Chat Completions API.
 */
interface OpenAiApi {
    /**
     * Lightweight authenticated endpoint used for real credential validation.
     * All OpenAI-compatible providers (OpenAI, Groq, OpenRouter, Ollama) expose
     * GET /models with a Bearer token, so a 200 proves the key is usable.
     */
    @GET("models")
    suspend fun listModels(
        @Header("Authorization") authorization: String
    ): Response<ResponseBody>

    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest,
    ): Response<ChatCompletionResponse>

    @Streaming
    @POST("chat/completions")
    suspend fun createChatCompletionStream(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): Response<ResponseBody>
}

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Float? = null,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    val stream: Boolean = false,
)

@Serializable
data class OpenAiMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage? = null
) {
    @Serializable
    data class Choice(
        val message: OpenAiMessage,
        @SerialName("finish_reason") val finishReason: String? = null
    )

    @Serializable
    data class Usage(
        @SerialName("prompt_tokens") val promptTokens: Int,
        @SerialName("completion_tokens") val completionTokens: Int,
        @SerialName("total_tokens") val totalTokens: Int
    )
}

@Serializable
data class ChatCompletionChunk(
    val id: String,
    val choices: List<ChunkChoice>
) {
    @Serializable
    data class ChunkChoice(
        val delta: Delta,
        @SerialName("finish_reason") val finishReason: String? = null
    )

    @Serializable
    data class Delta(
        val role: String? = null,
        val content: String? = null
    )
}
