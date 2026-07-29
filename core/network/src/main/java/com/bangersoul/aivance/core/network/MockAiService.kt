package com.bangersoul.aivance.core.network

import com.bangersoul.aivance.core.network.ai.AiMessage
import com.bangersoul.aivance.core.network.ai.AiRole
import kotlinx.coroutines.delay
import javax.inject.Inject

class MockAiService @Inject constructor() : AiService {
    override suspend fun analyzeText(prompt: String): Result<String> {
        delay(1500)
        
        val response = when {
            prompt.contains("Analyze the following resume", ignoreCase = true) -> {
                """
                {
                  "matchScore": 75,
                  "keywords": [
                    {"text": "Kotlin", "isMatched": true},
                    {"text": "Jetpack Compose", "isMatched": true},
                    {"text": "Dagger Hilt", "isMatched": false},
                    {"text": "Unit Testing", "isMatched": false}
                  ],
                  "tips": [
                    {"category": "Skills", "description": "Add more details about your experience with Dagger Hilt."},
                    {"category": "Formatting", "description": "Ensure your contact information is clearly visible at the top."}
                  ]
                }
                """.trimIndent()
            }
            else -> "Mock response for: ${prompt.take(50)}..."
        }
        
        return Result.success(response)
    }

    override suspend fun chat(history: List<AiMessage>): Result<String> {
        delay(1000)
        val questions = listOf(
            "Tell me about a challenging project you worked on.",
            "How do you handle conflicts in a team?",
            "What are your strengths and weaknesses?",
            "Where do you see yourself in five years?",
            "Do you have any questions for us?"
        )

        // Use the number of assistant messages to determine the next question
        val assistantMessageCount = history.count { it.role == AiRole.Assistant }
        val response = questions.getOrElse(assistantMessageCount % questions.size) {
            "That's interesting. Can you elaborate more on that?"
        }

        return Result.success(response)
    }
}
