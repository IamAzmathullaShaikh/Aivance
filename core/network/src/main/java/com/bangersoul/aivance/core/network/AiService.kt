package com.bangersoul.aivance.core.network

import com.bangersoul.aivance.core.network.ai.AiMessage

interface AiService {
    suspend fun analyzeText(prompt: String): Result<String>
    suspend fun chat(history: List<AiMessage>): Result<String>
}
