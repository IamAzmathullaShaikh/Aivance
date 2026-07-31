package com.bangersoul.aivance.core.domain.analytics

import com.bangersoul.aivance.core.common.model.CareerRecommendation
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationEngine @Inject constructor(
    private val providerManager: ProviderManager
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun generateRecommendations(
        metricsSummary: String
    ): CoreResult<List<CareerRecommendation>> = runCatchingCore {
        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider available")

        val prompt = """
            Analyze the following career metrics and generate prioritized recommendations.

            Metrics Summary:
            ${metricsSummary}

            Return ONLY a JSON array of objects with fields:
            "title": String, "description": String, "priority": "HIGH"|"MEDIUM"|"LOW", "category": "RESUME"|"NETWORKING"|"INTERVIEW"|"GENERAL"
        """.trimIndent()

        val result = provider.generateText(prompt).getOrNull() ?: throw Exception("AI generation failed")

        val jsonText = if (result.contains("```json")) {
            result.substringAfter("```json").substringBefore("```").trim()
        } else if (result.contains("[") && result.contains("]")) {
            result.substring(result.indexOf("["), result.lastIndexOf("]") + 1)
        } else result

        json.decodeFromString<List<CareerRecommendation>>(jsonText)
    }
}
