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
        if (rawText.isBlank()) return emptyList()

        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider

        val aiSections = provider?.let {
            val prompt = """
                Parse the following raw resume text into a structured JSON format.
                Extract sections: "summary", "experience", "education", "skills", "projects", "certifications", "languages".

                Return ONLY a JSON array of objects with fields: "sectionType", "title", "content".
                "sectionType" must be one of the keys listed above.
                "content" should preserve the original text but formatted cleanly.

                Raw Text:
                $rawText
            """.trimIndent()

            val result = provider.generateText(prompt).getOrNull() ?: return@let emptyList()

            try {
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
        }.orEmpty()

        // Guarantee a usable result: when no AI provider is configured, the AI
        // call failed, or the model returned an unusable payload, fall back to a
        // deterministic heading-based splitter — never hand the Resume Engine an
        // empty section list (that surfaced as "Parsing failed — no sections").
        return if (aiSections.isNotEmpty()) aiSections else heuristicParse(rawText)
    }

    /**
     * Rule-based section splitter used when no AI provider is available.
     * Recognises common resume headings and splits the raw text into sections;
     * if no heading is found the entire text becomes a single "Summary" section
     * so the pipeline always has something to show.
     */
    private fun heuristicParse(rawText: String): List<ResumeSection> {
        val headings = listOf(
            "summary", "professional summary", "profile", "objective", "about",
            "experience", "work experience", "professional experience", "employment history", "career history",
            "education", "academic background", "qualifications",
            "skills", "technical skills", "core competencies", "technologies",
            "projects", "personal projects", "key projects", "publications",
            "certifications", "certificates", "licenses", "courses",
            "languages", "awards", "achievements", "honors", "interests", "references"
        )

        val lines = rawText.lines()
        if (lines.isEmpty()) return emptyList()

        val sections = mutableListOf<ResumeSection>()
        var currentTitle: String? = null
        val currentContent = StringBuilder()

        fun flush() {
            val title = currentTitle
            val content = currentContent.toString().trim()
            if (title != null && content.isNotEmpty()) {
                sections.add(
                    ResumeSection(
                        sectionType = "general",
                        title = title,
                        content = content,
                        sectionOrder = sections.size
                    )
                )
            }
            currentContent.clear()
        }

        lines.forEach { rawLine ->
            val line = rawLine.trim()
            if (line.isBlank()) return@forEach
            val normalized = line.lowercase().trimEnd(':', '.')
            if (normalized in headings) {
                flush()
                currentTitle = line.trimEnd(':', '.')
            } else if (currentTitle == null && sections.isEmpty() && currentContent.isEmpty()) {
                // Seed a title from the first non-heading line (often the name).
                currentTitle = line.take(60)
                // Don't append the seed line as content.
            } else {
                currentContent.append(line).append('\n')
            }
        }
        flush()

        if (sections.isEmpty()) {
            sections.add(
                ResumeSection(
                    sectionType = "general",
                    title = "Summary",
                    content = rawText.trim(),
                    sectionOrder = 0
                )
            )
        }
        return sections
    }
}
