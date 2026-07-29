package com.bangersoul.aivance.feature.interview.domain

data class InterviewFeedback(
    val summary: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val tips: List<String>
)
