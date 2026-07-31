package com.bangersoul.aivance.core.domain.workflow

import com.bangersoul.aivance.core.common.model.Application
import com.bangersoul.aivance.core.common.model.TimelineEvent
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.repository.ApplicationWorkflowRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkflowEngine @Inject constructor(
    private val repository: ApplicationWorkflowRepository
) {
    suspend fun transitionTo(application: Application, nextStageId: String): CoreResult<Unit> = runCatchingCore {
        if (application.currentStageId == nextStageId) return@runCatchingCore

        // 1. Validate transition (Rules can be added here)

        // 2. Update Application
        val updated = application.copy(
            currentStageId = nextStageId,
            lastModified = System.currentTimeMillis()
        )
        repository.saveApplication(updated)

        // 3. Log to Timeline
        repository.addTimelineEvent(
            TimelineEvent(
                applicationId = application.id,
                eventType = "STAGE_CHANGE",
                title = "Stage Changed",
                description = "Moved from ${application.currentStageId} to $nextStageId",
                metadata = mapOf("from" to application.currentStageId, "to" to nextStageId)
            )
        )

        // 4. Trigger Automations (via UseCase later)
    }
}
