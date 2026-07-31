package com.bangersoul.aivance.core.domain.usecase.interview

import com.bangersoul.aivance.core.common.model.InterviewFeedback
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Generates comprehensive feedback for an interview session.
 */
class GenerateFeedbackUseCase @Inject constructor(
    private val interviewRepository: InterviewRepository
) : UseCase<String, CoreResult<InterviewFeedback>>() {

    override suspend operator fun invoke(sessionId: String): CoreResult<InterviewFeedback> {
        return runCatchingCore {
            val sessionResult = interviewRepository.getSessionById(sessionId).firstOrNull()
            val session = (sessionResult as? Result.Success)?.data ?: throw Exception("Session not found")
            session.feedback ?: throw Exception("Feedback not yet generated")
        }
    }
}
