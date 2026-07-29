package com.bangersoul.aivance.feature.jobs.domain

import java.util.Date

data class JobListing(
    val id: String,
    val company: String,
    val title: String,
    val location: String,
    val type: String, // e.g., "Full-time", "Contract", "Remote"
    val salary: String,
    val description: String,
    val postedDate: Date
)
