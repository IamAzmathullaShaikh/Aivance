package com.bangersoul.aivance.core.domain.usecase.interview

import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class GenerateInterviewQuestionsRequest(
    val targetRole: String,
    val companyName: String = "",
    val difficulty: InterviewDifficulty = InterviewDifficulty.MEDIUM,
    val count: Int = 5
)

/**
 * Generates interview questions for practice.
 *
 * Business rules:
 * - Questions are tailored to the target role and company.
 * - Difficulty level affects question complexity.
 * - Returns a list of questions without answers.
 * - Maximum of 20 questions per request.
 */
class GenerateInterviewQuestionsUseCase @Inject constructor(
    private val aiRepository: AiRepository
) : UseCase<GenerateInterviewQuestionsRequest, CoreResult<List<String>>>() {

    override suspend operator fun invoke(input: GenerateInterviewQuestionsRequest): CoreResult<List<String>> {
        if (input.targetRole.isBlank()) {
            return Result.Failure(ValidationError("targetRole", "Target role cannot be blank."))
        }
        if (input.count <= 0 || input.count > 20) {
            return Result.Failure(ValidationError("count", "Question count must be between 1 and 20."))
        }

        return runCatchingCore {
            val prompt = buildString {
                append("Generate $input.count interview questions for a $input.targetRole position")
                if (input.companyName.isNotBlank()) {
                    append(" at ${input.companyName}")
                }
                append(" at $input.difficulty difficulty level.")
                append(" Return only the questions, one per line, numbered.")
            }

            val result = aiRepository.analyzeText(
                text = input.targetRole,
                prompt = prompt
            )

            when (result) {
                is Result.Success -> {
                    result.data.lines()
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .map { it.replace(Regex("^\\d+[\\.\\s]+"), "") }
                        .take(input.count)
                }
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }
}
