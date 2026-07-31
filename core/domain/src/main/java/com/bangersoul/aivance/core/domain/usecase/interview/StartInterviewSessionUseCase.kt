package com.bangersoul.aivance.core.domain.usecase.interview

import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.ValidationError
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

data class StartInterviewSessionRequest(
    val targetRole: String,
    val companyName: String = "",
    val difficulty: InterviewDifficulty = InterviewDifficulty.MEDIUM,
    val jobId: Long? = null,
    val resumeVersionId: Long? = null,
    val type: String = "BEHAVIORAL"
)

/**
 * Starts a new mock interview session.
 */
class StartInterviewSessionUseCase @Inject constructor(
    private val interviewRepository: InterviewRepository
) : UseCase<StartInterviewSessionRequest, CoreResult<InterviewSession>>() {

    override suspend operator fun invoke(input: StartInterviewSessionRequest): CoreResult<InterviewSession> {
        if (input.targetRole.isBlank()) {
            return Result.Failure(ValidationError("targetRole", "Target role cannot be blank."))
        }

        return runCatchingCore {
            val result = interviewRepository.startSession(
                role = input.targetRole,
                company = input.companyName,
                difficulty = input.difficulty,
                jobId = input.jobId,
                resumeVersionId = input.resumeVersionId,
                type = input.type
            )
            when (result) {
                is Result.Success -> result.data
                is Result.Failure -> throw Exception(result.error.message)
            }
        }
    }
}
