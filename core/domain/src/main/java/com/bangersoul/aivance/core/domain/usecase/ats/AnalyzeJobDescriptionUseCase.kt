package com.bangersoul.aivance.core.domain.usecase.ats

import com.bangersoul.aivance.core.common.model.JobDescription
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AtsRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Parses a raw job description string into a structured model.
 */
class AnalyzeJobDescriptionUseCase @Inject constructor(
    private val atsRepository: AtsRepository,
    private val providerManager: ProviderManager
) : UseCase<String, CoreResult<Long>>() {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend operator fun invoke(input: String): CoreResult<Long> = runCatchingCore {
        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider available")

        val prompt = """
            Parse the following job description into a structured JSON format.
            Extract: "companyName", "jobTitle", "extractedSkills" (list of strings).

            Return ONLY the JSON object.

            Job Description:
            $input
        """.trimIndent()

        val result = provider.generateText(prompt).getOrNull() ?: throw Exception("AI parsing failed")

        val jsonText = if (result.contains("```json")) {
            result.substringAfter("```json").substringBefore("```").trim()
        } else if (result.contains("{") && result.contains("}")) {
            val start = result.indexOf("{")
            val end = result.lastIndexOf("}")
            result.substring(start, end + 1)
        } else result

        val parsedJd = json.decodeFromString<JobDescription>(jsonText).copy(rawText = input)

        val saveResult = atsRepository.saveJobDescription(parsedJd)
        when (saveResult) {
            is com.bangersoul.aivance.core.common.result.Result.Success -> saveResult.data
            is com.bangersoul.aivance.core.common.result.Result.Failure -> throw Exception(saveResult.error.message)
        }
    }
}
