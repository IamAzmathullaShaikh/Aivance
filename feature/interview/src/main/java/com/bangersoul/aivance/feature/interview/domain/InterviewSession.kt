package com.bangersoul.aivance.feature.interview.domain

data class InterviewSession(
    val id: String,
    val role: String,
    val difficulty: String,
    val messages: List<InterviewMessage> = emptyList(),
    val feedback: InterviewFeedback? = null
)
