package com.bangersoul.aivance.feature.ats.domain

import java.time.Instant

data class AtsResult(
    val id: Long = 0,
    val score: Int,
    val date: Instant,
    val resumeName: String,
    val missingKeywords: List<String>,
    val feedback: String
)
