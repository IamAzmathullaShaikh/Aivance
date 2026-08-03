package com.bangersoul.aivance.core.domain.usecase.workflow

import com.bangersoul.aivance.core.common.model.Application
import com.bangersoul.aivance.core.common.model.ApplicationTask
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.ApplicationWorkflowRepository
import com.bangersoul.aivance.core.domain.usecase.UseCase
import javax.inject.Inject

/**
 * Automatically generates tasks for an application based on its current state.
 */
class TaskGeneratorUseCase @Inject constructor(
    private val repository: ApplicationWorkflowRepository
) : UseCase<Application, CoreResult<Unit>>() {

    override suspend operator fun invoke(input: Application): CoreResult<Unit> = runCatchingCore {
        when (input.currentStageId.uppercase()) {
            "SAVED" -> {
                if (input.atsReportId == null) {
                    repository.addTask(
                        ApplicationTask(
                            applicationId = input.id,
                            title = "Analyze ATS Match",
                            description = "Scan your resume against this job description to identify gaps.",
                            priority = "HIGH"
                        )
                    )
                }
            }
            "PREPARING" -> {
                if (input.coverLetterVersionId == null) {
                    repository.addTask(
                        ApplicationTask(
                            applicationId = input.id,
                            title = "Generate Cover Letter",
                            description = "Create a personalized cover letter using AI match insights.",
                            priority = "MEDIUM"
                        )
                    )
                }
                if (input.resumeVersionId == null) {
                    repository.addTask(
                        ApplicationTask(
                            applicationId = input.id,
                            title = "Tailor Resume",
                            description = "Optimize your resume sections for this specific role.",
                            priority = "HIGH"
                        )
                    )
                }
            }
            "APPLIED" -> {
                repository.addTask(
                    ApplicationTask(
                        applicationId = input.id,
                        title = "Research Company",
                        description = "Deep dive into ${input.job?.company ?: "the company"}'s culture and tech stack.",
                        priority = "MEDIUM"
                    )
                )
                repository.addTask(
                    ApplicationTask(
                        applicationId = input.id,
                        title = "Schedule Follow-up",
                        description = "Set a reminder to reach out if you don't hear back in 7 days.",
                        priority = "LOW"
                    )
                )
            }
            "INTERVIEW", "INTERVIEWING" -> {
                repository.addTask(
                    ApplicationTask(
                        applicationId = input.id,
                        title = "Mock Interview Practice",
                        description = "Start a practice session for the ${input.job?.title ?: "role"}.",
                        priority = "HIGH"
                    )
                )
            }
        }

        // Add AI-suggested task if any high-priority recommendations exist
        // (In a real implementation, we would pass the full CareerState here)
    }
}
