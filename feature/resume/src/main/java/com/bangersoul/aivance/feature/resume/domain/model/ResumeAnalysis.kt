package com.bangersoul.aivance.feature.resume.domain.model

data class ResumeAnalysis(
    val matchScore: Int,
    val keywords: List<KeywordInfo>,
    val tips: List<OptimizationTip>
)
