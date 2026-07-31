package com.bangersoul.aivance.core.domain.assistant

import com.bangersoul.aivance.core.common.model.*
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.domain.repository.*
import com.bangersoul.aivance.core.domain.repository.crm.RecruiterIntelligenceRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssistantContextEngine @Inject constructor(
    private val resumeRepository: ResumeRepository,
    private val atsRepository: AtsRepository,
    private val workflowRepository: ApplicationWorkflowRepository,
    private val analyticsRepository: AnalyticsRepository
) {
    suspend fun buildActiveContext(): String {
        val resumes = resumeRepository.getResumes().firstOrNull()?.getOrNull() ?: emptyList()
        val latestAnalyses = analyticsRepository.getSnapshots().firstOrNull()?.getOrNull() ?: emptyList()
        val apps = workflowRepository.getApplications().firstOrNull()?.getOrNull() ?: emptyList()

        return buildString {
            appendLine("User Career Context:")
            appendLine("- Total Resumes: ${resumes.size}")
            appendLine("- Active Applications: ${apps.count { it.status == "ACTIVE" }}")
            appendLine("- Latest Career Score: ${latestAnalyses.firstOrNull()?.careerScore ?: "N/A"}")

            if (apps.isNotEmpty()) {
                val topApp = apps.maxBy { it.lastModified }
                appendLine("- Top Focus: ${topApp.job?.title} at ${topApp.job?.company} (${topApp.currentStageId})")
            }
        }
    }
}
