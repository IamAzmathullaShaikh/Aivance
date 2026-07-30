package com.bangersoul.aivance.core.domain.usecase.career

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class RecommendSkillsRequest(
    val targetRole: String,
    val currentSkills: List<String> = emptyList(),
    val industry: String = ""
)

/**
 * Recommends skills to learn for career advancement.
 *
 * Business rules:
 * - Target role must be provided.
 * - Considers current skills to avoid redundant recommendations.
 * - Returns skills ranked by relevance to the target role.
 * - Separates technical and soft skills.
 */
class RecommendSkillsUseCase @Inject constructor(
    private val aiRepository: AiRepository
) : UseCase<RecommendSkillsRequest, CoreResult<List<SkillRecommendation>>>() {

    override suspend operator fun invoke(input: RecommendSkillsRequest): CoreResult<List<SkillRecommendation>> {
        if (input.targetRole.isBlank()) {
            return Result.Failure(ValidationError("targetRole", "Target role cannot be blank."))
        }

        return runCatchingCore {
            val prompt = buildString {
                appendLine("Recommend skills for a $input.targetRole position.")
                if (input.industry.isNotBlank()) {
                    appendLine("Industry: ${input.industry}")
                }
                if (input.currentSkills.isNotEmpty()) {
                    appendLine("Current skills (avoid recommending these): ${input.currentSkills.joinToString(", ")}")
                }
                appendLine()
                appendLine("Format each recommendation as:")
                appendLine("SKILL: <name> | CATEGORY: <Technical/Soft> | PRIORITY: <High/Medium/Low> | REASON: <why important>")
            }

            val result = aiRepository.analyzeText(
                text = input.targetRole,
                prompt = prompt
            )

            when (result) {
                is Result.Success -> parseRecommendations(result.data)
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }

    private fun parseRecommendations(response: String): List<SkillRecommendation> {
        return response.lines()
            .mapNotNull { line ->
                val skillMatch = Regex("SKILL:\\s*(.*?)(?:\\||\$)").find(line)
                val categoryMatch = Regex("CATEGORY:\\s*(.*?)(?:\\||\$)").find(line)
                val priorityMatch = Regex("PRIORITY:\\s*(.*?)(?:\\||\$)").find(line)
                val reasonMatch = Regex("REASON:\\s*(.*)").find(line)

                val name = skillMatch?.groupValues?.get(1)?.trim() ?: return@mapNotNull null

                SkillRecommendation(
                    name = name,
                    category = categoryMatch?.groupValues?.get(1)?.trim() ?: "Technical",
                    priority = priorityMatch?.groupValues?.get(1)?.trim() ?: "Medium",
                    reason = reasonMatch?.groupValues?.get(1)?.trim() ?: ""
                )
            }
    }
}

data class SkillRecommendation(
    val name: String,
    val category: String,
    val priority: String,
    val reason: String
)
