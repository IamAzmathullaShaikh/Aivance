package com.bangersoul.aivance.ai.anthropic

import com.bangersoul.aivance.ai.anthropic.models.ClaudeMessage
import com.bangersoul.aivance.ai.anthropic.models.ClaudeMessageRequest
import com.bangersoul.aivance.ai.anthropic.models.ClaudeStreamEvent
import com.bangersoul.aivance.core.common.enums.MessageRole
import com.bangersoul.aivance.core.common.result.ProviderError
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import timber.log.Timber

/**
 * Anthropic Claude AI Provider implementation.
 */
class ClaudeProvider(
    private var config: ProviderConfiguration,
) : AIProvider(
    metadata = ProviderMetadata(
        id = "anthropic",
        name = "Anthropic Claude",
        type = ProviderType.AI,
        version = "1.0.0",
        description = "Anthropic's powerful AI models like Claude 3.5 Sonnet.",
        author = "Anthropic",
        configFields = listOf(
            ConfigField(
                key = "apiKey",
                label = "Anthropic API Key",
                isSensitive = true,
                fieldType = FieldType.PASSWORD
            )
        ),
        supportedModels = listOf("claude-3-5-sonnet-20240620", "claude-3-opus-20240229", "claude-3-haiku-20240307")
    ),
    capabilities = setOf(
        ProviderCapability.AI.Chat,
        ProviderCapability.AI.TextGeneration,
        ProviderCapability.AI.Streaming
    )
) {
    private lateinit var api: ClaudeApi
    private val json = Json { ignoreUnknownKeys = true }

    override val isConfigured: Boolean
        get() = (config.secrets["apiKey"] ?: "").isNotBlank()

    override val hasCredentials: Boolean
        get() = isConfigured

    override suspend fun applyConfiguration(config: ProviderConfiguration) {
        // Only swap the configuration; the caller owns re-initialization.
        this.config = config
    }

    private val baseUrl: String
        get() = config.settings["baseUrl"] ?: "https://api.anthropic.com/"

    private val modelName: String
        get() = config.settings["model"] ?: "claude-3-5-sonnet-20240620"

    private val apiKey: String
        get() = config.secrets["apiKey"] ?: ""

    private val anthropicVersion: String
        get() = config.settings["anthropic-version"] ?: "2023-06-01"

    override suspend fun onInitialize() {
        updateStatus(ProviderStatus.Initializing)
        try {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl.ensureTrailingSlash())
                .client(client)
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()

            api = retrofit.create(ClaudeApi::class.java)
            if (apiKey.isBlank()) {
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
        return try {
            val (systemPrompt, claudeMessages) = messages.toClaudeFormat()
            val request = ClaudeMessageRequest(
                model = modelName,
                messages = claudeMessages,
                system = systemPrompt,
                temperature = config.settings["temperature"]?.toFloatOrNull()
            )

            val response = api.createMessage(apiKey, anthropicVersion, request)
            if (response.isSuccessful) {
                val body = response.body()
                val content = body?.content?.firstOrNull()?.text ?: ""
                Result.Success(content)
            } else {
                Result.Failure(
                    ProviderError(
                        providerId = metadata.id,
                        statusCode = response.code(),
                        message = "API Error: ${response.message()}"
                    )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in chat for ${metadata.name}")
            Result.Failure(
                ProviderError(
                    providerId = metadata.id,
                    message = e.message ?: "Chat failed",
                    cause = e
                )
            )
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
        val (systemPrompt, claudeMessages) = messages.toClaudeFormat()
        val request = ClaudeMessageRequest(
            model = modelName,
            messages = claudeMessages,
            system = systemPrompt,
            temperature = config.settings["temperature"]?.toFloatOrNull(),
            stream = true
        )

        val response = api.createMessageStream(apiKey, anthropicVersion, request)
        if (response.isSuccessful) {
            val responseBody = response.body() ?: throw Exception("Empty response body")
            responseBody.source().use { source ->
                while (!source.exhausted()) {
                    val line = source.readUtf8Line() ?: break
                    if (line.startsWith("data: ")) {
                        val data = line.substring(6).trim()

                        try {
                            val event = json.decodeFromString<ClaudeStreamEvent>(data)
                            if (event.type == "content_block_delta") {
                                val text = event.delta?.text
                                if (!text.isNullOrEmpty()) {
                                    emit(Result.Success(text))
                                }
                            }
                        } catch (e: Exception) {
                            // Some events might not be parseable as ClaudeStreamEvent if they have different structure
                            // but we are mostly interested in content_block_delta
                            Timber.d(e, "Skipping SSE chunk: $data")
                        }
                    }
                }
            }
        } else {
            emit(
                Result.Failure(
                    ProviderError(
                        providerId = metadata.id,
                        statusCode = response.code(),
                        message = "Streaming API Error"
                    )
                )
            )
        }
    }.catch { e ->
        emit(
            Result.Failure(
                ProviderError(
                    providerId = metadata.id,
                    message = e.message ?: "Streaming failed",
                    cause = e
                )
            )
        )
    }

    override suspend fun listModels(): Result<List<String>> {
        return Result.Success(listOf("claude-3-5-sonnet-20240620", "claude-3-opus-20240229", "claude-3-haiku-20240307"))
    }

    private fun List<AiMessage>.toClaudeFormat(): Pair<String?, List<ClaudeMessage>> {
        val systemPrompt = this.filter { it.role == MessageRole.SYSTEM }
            .joinToString("\n") { it.content }
            .takeIf { it.isNotEmpty() }

        val claudeMessages = this.filter { it.role != MessageRole.SYSTEM }
            .map { it.toClaudeMessage() }

        return Pair(systemPrompt, claudeMessages)
    }

    private fun AiMessage.toClaudeMessage(): ClaudeMessage {
        return ClaudeMessage(
            role = role.toClaudeRole(),
            content = content
        )
    }

    private fun MessageRole.toClaudeRole(): String = when (this) {
        MessageRole.USER -> "user"
        MessageRole.ASSISTANT -> "assistant"
        // Claude expects system prompts separately, and doesn't support 'system' role in messages
        // Other roles are mapped to user for safety if they appear here
        else -> "user"
    }

    private fun String.ensureTrailingSlash(): String {
        return if (endsWith("/")) this else "$this/"
    }
}
