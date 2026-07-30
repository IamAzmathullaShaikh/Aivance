package com.bangersoul.aivance.core.domain.usecase.interview

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

/**
 * Ends an ongoing interview session.
 *
 * Business rules:
 * - Session must exist and be in progress.
 * - Marks the session as completed.
 * - Generates final feedback before ending.
 * - Session data is preserved for historical review.
 */
class EndInterviewUseCase @Inject constructor(
    private val interviewRepository: InterviewRepository
) : UseCase<String, CoreResult<Unit>>() {

    override suspend operator fun invoke(sessionId: String): CoreResult<Unit> {
        if (sessionId.isBlank()) {
            return Result.Failure(ValidationError("sessionId", "Session ID cannot be blank."))
        }

        return runCatchingCore {
            interviewRepository.completeSession(sessionId)
        }
    }
}
