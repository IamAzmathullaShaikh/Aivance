package com.bangersoul.aivance.core.domain.analytics

import com.bangersoul.aivance.core.common.model.*
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

    fun getStaticRecommendations(state: CareerState): List<CareerRecommendation> {
        return buildList {
            if (state.profile.targetRole.isEmpty()) {
                add(CareerRecommendation(
                    title = "Complete your Profile",
                    description = "Add your target role to get personalized job matches.",
                    category = "SYSTEM",
                    priority = "HIGH",
                    actionDeepLink = "aivance://profile"
                ))
            }
            if (state.intelligence.totalResumes == 0) {
                add(CareerRecommendation(
                    title = "Upload Resume",
                    description = "Upload your resume to start analyzing matches.",
                    category = "RESUME",
                    priority = "HIGH",
                    actionDeepLink = "aivance://resume/import"
                ))
            }
        }
    }

    suspend fun generateRecommendations(
        metricsSummary: String
    ): CoreResult<List<CareerRecommendation>> = runCatchingCore {
        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider available")

        val prompt = """
            Analyze these career metrics: $metricsSummary.
            Generate prioritized career recommendations in JSON format.
            Each recommendation must include a 'description' that explains WHY it was generated based on the metrics.
        """.trimIndent()
        val result = provider.generateText(prompt).getOrNull() ?: throw Exception("AI generation failed")
        val jsonText = if (result.contains("[")) result.substring(result.indexOf("["), result.lastIndexOf("]") + 1) else result
        json.decodeFromString<List<CareerRecommendation>>(jsonText)
    }

    suspend fun generateAiRecommendations(
        state: CareerState
    ): CoreResult<List<CareerRecommendation>> = runCatchingCore {
        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider available")

        val prompt = """
            Analyze the following career state and generate prioritized recommendations.

            Career State:
            ${state}

            Return ONLY a JSON array of objects with fields:
            "title": String, "description": String, "priority": "HIGH"|"MEDIUM"|"LOW", "category": "RESUME"|"NETWORKING"|"INTERVIEW"|"GENERAL", "actionDeepLink": String
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
