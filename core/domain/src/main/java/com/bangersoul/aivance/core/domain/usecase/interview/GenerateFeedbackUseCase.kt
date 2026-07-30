package com.bangersoul.aivance.core.domain.usecase.interview

import com.bangersoul.aivance.core.common.model.InterviewFeedback
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

/**
 * Generates comprehensive feedback for a completed interview session.
 *
 * Business rules:
 * - Session must exist and be completed.
 * - Feedback includes overall score, strengths, and improvement areas.
 * - Uses the conversation history to generate accurate feedback.
 * - Feedback is persisted with the session.
 */
class GenerateFeedbackUseCase @Inject constructor(
    private val interviewRepository: InterviewRepository
) : UseCase<String, CoreResult<InterviewFeedback>>() {

    override suspend operator fun invoke(sessionId: String): CoreResult<InterviewFeedback> {
        if (sessionId.isBlank()) {
            return Result.Failure(ValidationError("sessionId", "Session ID cannot be blank."))
        }

        return runCatchingCore {
            val result = interviewRepository.generateFeedback(sessionId)
            when (result) {
                is Result.Success -> result.data
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }
}
