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
        when (input.currentStageId) {
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
            }
        }
    }
}
