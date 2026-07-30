package com.bangersoul.aivance.core.domain.usecase.career

import com.bangersoul.aivance.core.common.model.CareerRoadmap
import com.bangersoul.aivance.core.common.model.RoadmapStep
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class GenerateCareerRoadmapRequest(
    val targetRole: String,
    val currentSkills: List<String> = emptyList(),
    val experienceYears: Int = 0,
    val currentLevel: String = "Entry Level"
)

/**
 * Generates a career roadmap with actionable steps.
 *
 * Business rules:
 * - Target role must be provided.
 * - Uses AI to create a structured multi-step career progression plan.
 * - Each step includes a title, description, and suggested order.
 * - Roadmap is tailored based on current skills and experience.
 */
class GenerateCareerRoadmapUseCase @Inject constructor(
    private val aiRepository: AiRepository
) : UseCase<GenerateCareerRoadmapRequest, CoreResult<CareerRoadmap>>() {

    override suspend operator fun invoke(input: GenerateCareerRoadmapRequest): CoreResult<CareerRoadmap> {
        if (input.targetRole.isBlank()) {
            return Result.Failure(ValidationError("targetRole", "Target role cannot be blank."))
        }

        return runCatchingCore {
            val prompt = buildString {
                appendLine("Create a career roadmap for someone targeting a ${input.targetRole} role.")
                appendLine("Current level: ${input.currentLevel}")
                appendLine("Years of experience: ${input.experienceYears}")
                if (input.currentSkills.isNotEmpty()) {
                    appendLine("Current skills: ${input.currentSkills.joinToString(", ")}")
                }
                appendLine()
                appendLine("Provide the roadmap as a numbered list of steps.")
                appendLine("For each step, include a title and brief description.")
            }

            val result = aiRepository.analyzeText(
                text = "${input.currentSkills.joinToString(", ")}",
                prompt = prompt
            )

            when (result) {
                is Result.Success -> {
                    val steps = parseSteps(result.data)
                    CareerRoadmap(
                        targetRole = input.targetRole,
                        currentLevel = input.currentLevel,
                        description = "Career progression plan for $input.targetRole",
                        steps = steps
                    )
                }
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }

    private fun parseSteps(response: String): List<RoadmapStep> {
        return response.lines()
            .map { it.trim() }
            .filter { it.matches(Regex("^\\d+[\\.,\\)]\\s*.+")) || it.startsWith("- ") || it.startsWith("* ") }
            .mapIndexed { index, line ->
                val title = line
                    .replace(Regex("^\\d+[\\.,\\)]\\s*"), "")
                    .replace(Regex("^[-\\*]\\s*"), "")
                    .trim()
                RoadmapStep(
                    title = title.take(100),
                    description = title,
                    stepOrder = index + 1
                )
            }
    }
}
