package com.bangersoul.aivance.core.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Application(
    val id: Long = 0,
    val jobId: Long,
    val resumeVersionId: Long? = null,
    val atsReportId: Long? = null,
    val coverLetterVersionId: Long? = null,
    val currentStageId: String = "SAVED",
    val status: String = "ACTIVE",
    val dateApplied: Long? = null,
    val lastModified: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val job: JobListing? = null,
    val timeline: List<TimelineEvent> = emptyList(),
    val tasks: List<ApplicationTask> = emptyList()
)

@Serializable
data class ApplicationStage(
    val id: String,
    val label: String,
    val order: Int,
    val isSystemStage: Boolean = true
)

@Serializable
data class TimelineEvent(
    val id: Long = 0,
    val applicationId: Long,
    val eventType: String,
    val title: String,
    val description: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class ApplicationTask(
    val id: Long = 0,
    val applicationId: Long,
    val title: String,
    val description: String?,
    val priority: String = "MEDIUM",
    val dueDate: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)
