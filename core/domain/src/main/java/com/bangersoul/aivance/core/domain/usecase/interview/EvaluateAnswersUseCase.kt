package com.bangersoul.aivance.core.domain.usecase.interview

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.AiRepository
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class EvaluateAnswersRequest(
    val sessionId: String,
    val question: String,
    val answer: String
)

data class AnswerEvaluation(
    val score: Int,
    val feedback: String,
    val suggestions: List<String> = emptyList()
)

/**
 * Evaluates a user's answer to an interview question.
 *
 * Business rules:
 * - Session must exist.
 * - Question and answer must not be blank.
 * - Score ranges from 0 to 100.
 * - Provides constructive feedback and improvement suggestions.
 */
class EvaluateAnswersUseCase @Inject constructor(
    private val interviewRepository: InterviewRepository,
    private val aiRepository: AiRepository
) : UseCase<EvaluateAnswersRequest, CoreResult<AnswerEvaluation>>() {

    override suspend operator fun invoke(input: EvaluateAnswersRequest): CoreResult<AnswerEvaluation> {
        if (input.sessionId.isBlank()) {
            return Result.Failure(ValidationError("sessionId", "Session ID cannot be blank."))
        }
        if (input.question.isBlank()) {
            return Result.Failure(ValidationError("question", "Question cannot be blank."))
        }
        if (input.answer.isBlank()) {
            return Result.Failure(ValidationError("answer", "Answer cannot be blank."))
        }

        return runCatchingCore {
            val prompt = buildString {
                appendLine("Evaluate this interview answer:")
                appendLine()
                appendLine("Question: ${input.question}")
                appendLine()
                appendLine("Answer: ${input.answer}")
                appendLine()
                appendLine("Provide a score out of 100, feedback, and suggestions for improvement.")
                appendLine("Format: SCORE: <number> | FEEDBACK: <text> | SUGGESTIONS: <comma-separated>")
            }

            val result = aiRepository.analyzeText(text = input.answer, prompt = prompt)

            when (result) {
                is Result.Success -> parseEvaluation(result.data)
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }

    private fun parseEvaluation(response: String): AnswerEvaluation {
        val score = Regex("SCORE:\\s*(\\d+)").find(response)
            ?.groupValues?.get(1)?.toIntOrNull()?.coerceIn(0, 100) ?: 50

        val feedback = Regex("FEEDBACK:\\s*(.*?)(?:\\||\$)").find(response)
            ?.groupValues?.get(1)?.trim() ?: "Review the answer and try to be more specific."

        val suggestionsText = Regex("SUGGESTIONS:\\s*(.*)").find(response)
            ?.groupValues?.get(1)?.trim()

        val suggestions = suggestionsText
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        return AnswerEvaluation(
            score = score,
            feedback = feedback,
            suggestions = suggestions
        )
    }
}
