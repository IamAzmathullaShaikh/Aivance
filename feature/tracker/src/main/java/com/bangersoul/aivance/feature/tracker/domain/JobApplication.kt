package com.bangersoul.aivance.feature.tracker.domain

import java.time.Instant

data class JobApplication(
    val id: Long = 0,
    val company: String,
    val role: String,
    val status: ApplicationStatus,
    val dateApplied: Instant,
    val salaryRange: String?,
    val notes: String?,
    val lastModified: Instant
)
