package com.bangersoul.aivance.core.network

import android.content.Context
import com.bangersoul.aivance.core.datastore.UserPreferencesRepository
import com.bangersoul.aivance.core.network.ai.AiMessage
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.app
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DelegatingAiService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val mockAiService: MockAiService,
) : AiService {

    private suspend fun getActiveService(): AiService {
        val prefs = userPreferencesRepository.userPreferences.first()
        val apiKey = prefs.geminiApiKey?.takeIf { it.isNotEmpty() } ?: BuildConfig.GEMINI_API_KEY
        
        return if (apiKey.isNotEmpty()) {
            val aiApp = getAiApp(apiKey)
            GeminiAiService(
                Firebase.ai(app = aiApp, backend = GenerativeBackend.googleAI())
                    .generativeModel(
                        modelName = "gemini-2.5-flash",
                    )
            )
        } else {
            mockAiService
        }
    }

    private fun getAiApp(apiKey: String): FirebaseApp {
        val appName = "AiApp"
        return try {
            val app = FirebaseApp.getInstance(appName)
            if (app.options.apiKey == apiKey) {
                app
            } else {
                app.delete()
                initializeApp(apiKey, appName)
            }
        } catch (e: Exception) {
            initializeApp(apiKey, appName)
        }
    }

    private fun initializeApp(apiKey: String, name: String): FirebaseApp {
        val options = FirebaseOptions.Builder()
            .setApiKey(apiKey)
            .setApplicationId(Firebase.app.options.applicationId)
            .setProjectId(Firebase.app.options.projectId)
            .build()
        return FirebaseApp.initializeApp(context, options, name)
    }

    override suspend fun analyzeText(prompt: String): Result<String> {
        return getActiveService().analyzeText(prompt)
    }

    override suspend fun chat(history: List<AiMessage>): Result<String> {
        return getActiveService().chat(history)
    }
}
