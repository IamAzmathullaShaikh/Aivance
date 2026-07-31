package com.bangersoul.aivance.core.data.resume

import com.bangersoul.aivance.core.common.model.ResumeSection
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResumeParser @Inject constructor(
    private val providerManager: ProviderManager
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun parseRawText(rawText: String): List<ResumeSection> {
        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: return emptyList()

        val prompt = """
            Parse the following raw resume text into a structured JSON format.
            Extract sections: "summary", "experience", "education", "skills", "projects", "certifications", "languages".

            Return ONLY a JSON array of objects with fields: "sectionType", "title", "content".
            "sectionType" must be one of the keys listed above.
            "content" should preserve the original text but formatted cleanly.

            Raw Text:
            $rawText
        """.trimIndent()

        val result = provider.generateText(prompt).getOrNull() ?: return emptyList()

        return try {
            val jsonText = if (result.contains("```json")) {
                result.substringAfter("```json").substringBefore("```").trim()
            } else if (result.contains("[") && result.contains("]")) {
                val start = result.indexOf("[")
                val end = result.lastIndexOf("]")
                result.substring(start, end + 1)
            } else result

            val jsonElement = json.parseToJsonElement(jsonText)
            val jsonArray = jsonElement.jsonArray
            jsonArray.map { element ->
                val obj = element.jsonObject
                ResumeSection(
                    sectionType = obj["sectionType"]?.jsonPrimitive?.content ?: "general",
                    title = obj["title"]?.jsonPrimitive?.content ?: "Section",
                    content = obj["content"]?.jsonPrimitive?.content ?: ""
                )
            }
        } catch (e: Exception) {
            listOf(ResumeSection(sectionType = "general", title = "Imported Content", content = rawText))
        }
    }
}
