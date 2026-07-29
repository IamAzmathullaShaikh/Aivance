package com.bangersoul.aivance.core.network

import com.bangersoul.aivance.core.network.ai.AiMessage
import com.bangersoul.aivance.core.network.ai.AiRole
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.FirebaseAIException
import com.google.firebase.ai.type.InvalidAPIKeyException
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.ServerException
import com.google.firebase.ai.type.content
import timber.log.Timber
import javax.inject.Inject

class GeminiAiService @Inject constructor(
    private val generativeModel: GenerativeModel
) : AiService {
    override suspend fun analyzeText(prompt: String): Result<String> {
        return try {
            val response = generativeModel.generateContent(
                content {
                    text(prompt)
                }
            )
            Result.success(response.text ?: "")
        } catch (e: FirebaseAIException) {
            val errorMessage = handleAiException(e)
            Timber.e(e, errorMessage)
            Result.failure(Exception(errorMessage, e))
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error analyzing text with Gemini")
            Result.failure(e)
        }
    }

    override suspend fun chat(history: List<AiMessage>): Result<String> {
        return try {
            val geminiHistory = history.map { message ->
                content(role = when (message.role) {
                    AiRole.User -> "user"
                    AiRole.Assistant -> "model"
                    AiRole.System -> "system"
                }) {
                    text(message.content)
                }
            }

            val response = generativeModel.generateContent(geminiHistory)
            Result.success(response.text ?: "")
        } catch (e: FirebaseAIException) {
            val errorMessage = handleAiException(e)
            Timber.e(e, errorMessage)
            Result.failure(Exception(errorMessage, e))
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error in multi-turn chat with Gemini")
            Result.failure(e)
        }
    }

    private fun handleAiException(e: FirebaseAIException): String {
        return when (e) {
            is ServerException -> {
                if (e.message?.contains("404") == true || e.message?.contains("not found") == true) {
                    "The AI model (gemini-2.5-flash) is currently unavailable or has been retired. Please check for app updates."
                } else {
                    "AI server error: ${e.message}"
                }
            }
            is QuotaExceededException -> "AI quota exceeded. Please try again later."
            is InvalidAPIKeyException -> "Invalid AI API key. Please check your configuration."
            else -> "AI service error: ${e.message ?: "Unknown error"}"
        }
    }
}


