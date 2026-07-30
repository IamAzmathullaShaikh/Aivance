package com.bangersoul.aivance.core.domain.usecase.career

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class SuggestLearningPathRequest(
    val skillToLearn: String,
    val currentProficiency: String = "Beginner",
    val timeCommitment: String = "Part-time",
    val preferredResources: List<String> = emptyList()
)

data class LearningPathResource(
    val name: String,
    val type: String,
    val description: String,
    val estimatedDuration: String
)

data class LearningPath(
    val skill: String,
    val estimatedTimeToCompetency: String,
    val resources: List<LearningPathResource> = emptyList()
)

/**
 * Suggests a structured learning path for acquiring a specific skill.
 *
 * Business rules:
 * - Skill name must be provided.
 * - Considers current proficiency level for appropriate resource recommendations.
 * - Returns a curated list of learning resources with estimated durations.
 */
class SuggestLearningPathUseCase @Inject constructor(
    private val aiRepository: AiRepository
) : UseCase<SuggestLearningPathRequest, CoreResult<LearningPath>>() {

    override suspend operator fun invoke(input: SuggestLearningPathRequest): CoreResult<LearningPath> {
        if (input.skillToLearn.isBlank()) {
            return Result.Failure(ValidationError("skillToLearn", "Skill name cannot be blank."))
        }

        return runCatchingCore {
            val prompt = buildString {
                appendLine("Suggest a learning path for: ${input.skillToLearn}")
                appendLine("Current proficiency: ${input.currentProficiency}")
                appendLine("Time commitment: ${input.timeCommitment}")
                if (input.preferredResources.isNotEmpty()) {
                    appendLine("Preferred resource types: ${input.preferredResources.joinToString(", ")}")
                }
                appendLine()
                appendLine("Format each resource as:")
                appendLine("RESOURCE: <name> | TYPE: <course/book/video/tutorial> | DURATION: <time> | DESCRIPTION: <brief>")
                appendLine("Also estimate total time to competency.")
            }

            val result = aiRepository.analyzeText(
                text = input.skillToLearn,
                prompt = prompt
            )

            when (result) {
                is Result.Success -> parseLearningPath(result.data, input.skillToLearn)
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }

    private fun parseLearningPath(response: String, skill: String): LearningPath {
        val resources = response.lines()
            .mapNotNull { line ->
                val nameMatch = Regex("RESOURCE:\\s*(.*?)(?:\\||\$)").find(line)
                val typeMatch = Regex("TYPE:\\s*(.*?)(?:\\||\$)").find(line)
                val durationMatch = Regex("DURATION:\\s*(.*?)(?:\\||\$)").find(line)
                val descriptionMatch = Regex("DESCRIPTION:\\s*(.*)").find(line)

                val name = nameMatch?.groupValues?.get(1)?.trim() ?: return@mapNotNull null

                LearningPathResource(
                    name = name,
                    type = typeMatch?.groupValues?.get(1)?.trim() ?: "Course",
                    description = descriptionMatch?.groupValues?.get(1)?.trim() ?: "",
                    estimatedDuration = durationMatch?.groupValues?.get(1)?.trim() ?: "Varies"
                )
            }

        val estimatedTime = Regex("(?i)estimated?\\s*time\\s*(?:to\\s*)?competency?\\s*:?\\s*(.+?)(?:\$|\n)").find(response)
            ?.groupValues?.get(1)?.trim() ?: "3-6 months"

        return LearningPath(
            skill = skill,
            estimatedTimeToCompetency = estimatedTime,
            resources = resources
        )
    }
}
