package com.bangersoul.aivance.feature.ats.domain

import java.time.Instant

data class AtsResult(
    val id: Long = 0,
    val resumeId: Long,
    val jobDescription: String,
    val score: Int,
    val date: Instant,
    val matchedKeywords: List<String>,
    val missingKeywords: List<String>,
    val feedback: String
)
