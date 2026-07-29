package com.bangersoul.aivance.feature.ats.domain

data class AtsBreakdown(
    val score: Int,
    val missingKeywords: List<String>,
    val feedback: String
)
