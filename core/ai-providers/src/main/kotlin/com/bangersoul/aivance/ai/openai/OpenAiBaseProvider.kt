package com.bangersoul.aivance.ai.openai

import com.bangersoul.aivance.core.aiproviders.BuildConfig
import com.bangersoul.aivance.core.common.enums.MessageRole
import com.bangersoul.aivance.core.common.result.ProviderError
import com.bangersoul.aivance.core.common.security.CertificatePins
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.config.ProviderConfiguration
import com.bangersoul.aivance.sdk.core.ConfigField
import com.bangersoul.aivance.sdk.core.FieldType
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.core.ProviderMetadata
import com.bangersoul.aivance.sdk.core.ProviderStatus
import com.bangersoul.aivance.sdk.core.ProviderType
import com.bangersoul.aivance.sdk.model.AiMessage
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import timber.log.Timber

/**
 * Base provider for OpenAI-compatible APIs.
 */
abstract class OpenAiBaseProvider(
    metadata: ProviderMetadata,
    protected var config: ProviderConfiguration,
    private val defaultBaseUrl: String,
    protected open val defaultModel: String = "gpt-4o",
    protected open val requiresApiKey: Boolean = true
) : AIProvider(
    metadata = metadata,
    capabilities = setOf(
        ProviderCapability.AI.Chat,
        ProviderCapability.AI.TextGeneration,
        ProviderCapability.AI.Streaming
    )
) {
    protected lateinit var api: OpenAiApi
    protected val json = Json { ignoreUnknownKeys = true }

    private val baseUrl: String
        get() = config.settings["baseUrl"] ?: defaultBaseUrl

    private val modelName: String
        get() = config.settings["model"] ?: defaultModel

    protected val apiKey: String
        get() = config.secrets["apiKey"] ?: ""

    override val isConfigured: Boolean
        get() = !requiresApiKey || apiKey.isNotBlank()

    override val hasCredentials: Boolean
        get() = apiKey.isNotBlank()

    override suspend fun applyConfiguration(config: ProviderConfiguration) {
        // Only swap the configuration; the caller (validateProvider / reconfigure)
        // owns re-initialization, which rebuilds the Retrofit/API. This avoids
        // double-initialization churn.
        this.config = config
    }

    override suspend fun onInitialize() {
        updateStatus(ProviderStatus.Initializing)
        try {
            val logging = HttpLoggingInterceptor().apply {
                // Never log request/response bodies or auth headers outside debug
                // builds: release traffic carries Bearer API keys + full prompt/answer text.
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
                redactHeader("Authorization")
                redactHeader("x-api-key")
            }

            val pinnerBuilder = okhttp3.CertificatePinner.Builder()
            CertificatePins.OKHTTP_PINS.forEach { (host, pins) ->
                pinnerBuilder.add(host, *pins.toTypedArray())
            }

            val client = OkHttpClient.Builder()
                .certificatePinner(pinnerBuilder.build())
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl.ensureTrailingSlash())
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()

            api = retrofit.create(OpenAiApi::class.java)
            if (requiresApiKey && apiKey.isBlank()) {
                // Stay out of Ready until the user provides a real key, so AI
                // selection skips this provider instead of firing doomed 401s.
                updateStatus(ProviderStatus.InvalidConfiguration)
            } else {
                updateStatus(ProviderStatus.Ready)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize ${metadata.name}")
            updateStatus(ProviderStatus.Error)
        }
    }

    override suspend fun checkHealth(): ProviderStatus {
        if (!::api.isInitialized) {
            onInitialize()
        }
        if (!::api.isInitialized) {
            updateStatus(ProviderStatus.Error)
            return ProviderStatus.Error
        }
        return try {
            // Real authenticated request: a wrong key gets a 401 here, so
            // onboarding/settings validation genuinely rejects bad credentials
            // instead of blindly trusting a non-blank key.
            val response = api.listModels("Bearer $apiKey")
            if (response.isSuccessful) {
                updateStatus(ProviderStatus.Active)
                ProviderStatus.Active
            } else {
                Timber.w("Health check for ${metadata.name} failed: HTTP ${response.code()}")
                updateStatus(ProviderStatus.Error)
                ProviderStatus.Error
            }
        } catch (e: Exception) {
            Timber.w(e, "Health check for ${metadata.name} failed")
            updateStatus(ProviderStatus.Error)
            ProviderStatus.Error
        }
    }

    override suspend fun onStart() {
        if (status == ProviderStatus.Ready) {
            updateStatus(ProviderStatus.Active)
        }
    }

    override suspend fun onStop() {
        if (status == ProviderStatus.Active) {
            updateStatus(ProviderStatus.Ready)
        }
    }

    override suspend fun onDispose() {
        updateStatus(ProviderStatus.Disposed)
    }

    override suspend fun generateText(prompt: String): Result<String> {
        return chat(listOf(AiMessage(MessageRole.USER, prompt)))
    }

    override suspend fun chat(messages: List<AiMessage>): Result<String> {
        if (!::api.isInitialized) {
            onInitialize()
        }
        if (!::api.isInitialized) {
            return Result.Failure(ProviderError(providerId = metadata.id, message = "API not initialized"))
        }
        return try {
            val request = ChatCompletionRequest(
                model = modelName,
                messages = messages.map { it.toOpenAiMessage() },
                temperature = config.settings["temperature"]?.toFloatOrNull()
            )

            val response = api.createChatCompletion("Bearer $apiKey", request)
            if (response.isSuccessful) {
                val body = response.body()
                val content = body?.choices?.firstOrNull()?.message?.content ?: ""
                Result.Success(content)
            } else {
                Result.Failure(ProviderError(providerId = metadata.id, statusCode = response.code(), message = "API Error: ${response.message()}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in chat for ${metadata.name}")
            Result.Failure(ProviderError(providerId = metadata.id, message = e.message ?: "Chat failed", cause = e))
        }
    }

    override fun streamText(prompt: String): Flow<String> {
        return flow {
            streamChat(listOf(AiMessage(MessageRole.USER, prompt))).collect { result ->
                when (result) {
                    is Result.Success -> emit(result.data)
                    is Result.Failure -> throw Exception(result.error.message, result.error.cause)
                }
            }
        }
    }

    override fun streamChat(messages: List<AiMessage>): Flow<Result<String>> = flow {
        if (!::api.isInitialized) {
            onInitialize()
        }
        if (!::api.isInitialized) {
            emit(Result.Failure(ProviderError(providerId = metadata.id, message = "API not initialized")))
            return@flow
        }
        val request = ChatCompletionRequest(
            model = modelName,
            messages = messages.map { it.toOpenAiMessage() },
            temperature = config.settings["temperature"]?.toFloatOrNull(),
            stream = true
        )

        val response = api.createChatCompletionStream("Bearer $apiKey", request)
        if (response.isSuccessful) {
            val responseBody = response.body() ?: throw Exception("Empty response body")
            responseBody.source().use { source ->
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    if (line.startsWith("data: ")) {
                        val data = line.substring(6).trim()
                        if (data == "[DONE]") break

                        try {
                            val chunk = json.decodeFromString<ChatCompletionChunk>(data)
                            val content = chunk.choices.firstOrNull()?.delta?.content
                            if (!content.isNullOrEmpty()) {
                                emit(Result.Success(content))
                            }
                        } catch (e: Exception) {
                            Timber.w(e, "Failed to parse SSE chunk: $data")
                        }
                    }
                }
            }
        } else {
            emit(Result.Failure(ProviderError(providerId = metadata.id, statusCode = response.code(), message = "Streaming API Error")))
        }
    }.catch { e ->
        emit(Result.Failure(ProviderError(providerId = metadata.id, message = e.message ?: "Streaming failed", cause = e)))
    }

    override suspend fun listModels(): Result<List<String>> {
        // OpenAI compatibility varies for listing models, return default for now or implement if needed
        return Result.Success(listOf(modelName))
    }

    private fun AiMessage.toOpenAiMessage(): OpenAiMessage {
        return OpenAiMessage(
            role = role.toOpenAiRole(),
            content = content
        )
    }

    private fun MessageRole.toOpenAiRole(): String = when (this) {
        MessageRole.SYSTEM -> "system"
        MessageRole.USER -> "user"
        MessageRole.ASSISTANT -> "assistant"
        MessageRole.TOOL -> "tool"
    }

    private fun String.ensureTrailingSlash(): String {
        return if (endsWith("/")) this else "$this/"
    }
}
