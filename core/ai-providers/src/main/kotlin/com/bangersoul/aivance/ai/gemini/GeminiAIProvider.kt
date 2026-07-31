package com.bangersoul.aivance.ai.gemini

import android.content.Context
import com.bangersoul.aivance.core.common.enums.MessageRole
import com.bangersoul.aivance.core.common.result.DomainError
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
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.app
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * Gemini AI Provider implementation using Firebase AI SDK.
 */
class GeminiAIProvider(
    private val context: Context,
    private val config: ProviderConfiguration
) : AIProvider(
    metadata = ProviderMetadata(
        id = "gemini",
        name = "Google Gemini",
        type = ProviderType.AI,
        version = "1.0.0",
        description = "Powered by Google's most capable AI models via Firebase.",
        author = "Google",
        configFields = listOf(
            ConfigField(
                key = "apiKey",
                label = "Gemini API Key",
                isSensitive = true,
                fieldType = FieldType.PASSWORD,
                hint = "Get your key from Google AI Studio"
            )
        ),
        supportedModels = listOf("gemini-2.0-flash", "gemini-2.0-pro-exp-02-05", "gemini-1.5-flash", "gemini-1.5-pro")
    ),
    capabilities = setOf(
        ProviderCapability.AI.Chat,
        ProviderCapability.AI.TextGeneration,
        ProviderCapability.AI.Vision,
        ProviderCapability.AI.Streaming
    )
) {
    private var firebaseApp: FirebaseApp? = null
    private var _generativeModel: GenerativeModel? = null

    private val modelName: String
        get() = config.settings["model"] ?: "gemini-2.0-flash"

    override suspend fun onInitialize() {
        updateStatus(ProviderStatus.Initializing)
        try {
            val apiKey = config.secrets["apiKey"] ?: ""

            firebaseApp = if (apiKey.isNotEmpty()) {
                getDynamicFirebaseApp(apiKey)
            } else {
                Firebase.app
            }

            _generativeModel = createModel()

            updateStatus(ProviderStatus.Ready)
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize GeminiAIProvider")
            updateStatus(ProviderStatus.Error)
        }
    }

    private fun createModel(systemInstruction: String? = null): GenerativeModel {
        val app = firebaseApp ?: Firebase.app
        val modelInstruction = systemInstruction?.let {
            content { text(it) }
        }

        return Firebase.ai(app = app, backend = GenerativeBackend.googleAI())
            .generativeModel(
                modelName = modelName,
                systemInstruction = modelInstruction
            )
    }

    private fun getDynamicFirebaseApp(apiKey: String): FirebaseApp {
        val appName = "GeminiProviderApp_${config.providerId}"
        return try {
            val existingApp = FirebaseApp.getInstance(appName)
            if (existingApp.options.apiKey == apiKey) {
                existingApp
            } else {
                existingApp.delete()
                createNewFirebaseApp(apiKey, appName)
            }
        } catch (e: Exception) {
            createNewFirebaseApp(apiKey, appName)
        }
    }

    private fun createNewFirebaseApp(apiKey: String, name: String): FirebaseApp {
        val options = FirebaseOptions.Builder()
            .setApiKey(apiKey)
            .setApplicationId(Firebase.app.options.applicationId)
            .setProjectId(Firebase.app.options.projectId)
            .build()
        return FirebaseApp.initializeApp(context, options, name)
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
        _generativeModel = null
        firebaseApp = null
    }

    override suspend fun generateText(prompt: String): Result<String> {
        return try {
            val model = _generativeModel ?: return Result.Failure(DomainError("Provider not initialized"))
            val response = model.generateContent(prompt)
            Result.Success(response.text ?: "")
        } catch (e: Exception) {
            Timber.e(e, "Error generating text")
            Result.Failure(ProviderError(metadata.id, message = e.message ?: "Generation failed", cause = e))
        }
    }

    override suspend fun chat(messages: List<AiMessage>): Result<String> {
        return try {
            val systemMessage = messages.find { it.role == MessageRole.SYSTEM }
            val chatMessages = messages.filter { it.role != MessageRole.SYSTEM }

            val model = if (systemMessage != null) {
                createModel(systemMessage.content)
            } else {
                _generativeModel ?: return Result.Failure(DomainError("Provider not initialized"))
            }

            val chatHistory = chatMessages.dropLast(1).map { it.toGeminiContent() }
            val lastMessage = chatMessages.last().toGeminiContent()

            val chat = model.startChat(chatHistory)
            val response = chat.sendMessage(lastMessage)
            Result.Success(response.text ?: "")
        } catch (e: Exception) {
            Timber.e(e, "Error in chat")
            Result.Failure(ProviderError(metadata.id, message = e.message ?: "Chat failed", cause = e))
        }
    }

    override fun streamText(prompt: String): Flow<String> {
        val model = _generativeModel ?: throw IllegalStateException("Provider not initialized")
        return model.generateContentStream(prompt).map { it.text ?: "" }
    }

    override suspend fun listModels(): Result<List<String>> {
        return Result.Success(listOf("gemini-2.0-flash", "gemini-2.0-pro-exp-02-05", "gemini-1.5-flash", "gemini-1.5-pro"))
    }

    override fun streamChat(messages: List<AiMessage>): Flow<Result<String>> {
        val systemMessage = messages.find { it.role == MessageRole.SYSTEM }
        val chatMessages = messages.filter { it.role != MessageRole.SYSTEM }

        val model = if (systemMessage != null) {
            createModel(systemMessage.content)
        } else {
            _generativeModel ?: throw IllegalStateException("Provider not initialized")
        }

        val chatHistory = chatMessages.dropLast(1).map { it.toGeminiContent() }
        val lastMessage = chatMessages.last().toGeminiContent()

        val chat = model.startChat(chatHistory)
        return chat.sendMessageStream(lastMessage)
            .map { Result.Success(it.text ?: "") as Result<String> }
            .catch { e -> emit(Result.Failure(ProviderError(metadata.id, message = e.message ?: "Streaming failed", cause = e))) }
    }

    private fun AiMessage.toGeminiContent(): Content {
        val gRole = when (role) {
            MessageRole.USER -> "user"
            MessageRole.ASSISTANT -> "model"
            else -> "user"
        }
        return content(role = gRole) {
            text(content)
            // Vision mapping: add images if present
            images.forEach { base64Image ->
                // In a real implementation, we would decode the base64 or load the URI
                // For now, we represent the mapping logic
                // image(decodeBase64(base64Image))
            }
        }
    }
}
