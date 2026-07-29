package com.bangersoul.aivance.feature.interview.domain

import kotlinx.coroutines.flow.Flow

interface InterviewRepository {
    fun startSession(role: String, difficulty: String): Flow<InterviewMessage>
    fun sendMessage(text: String): Flow<InterviewMessage>
    fun getFeedback(): Flow<InterviewFeedback>
}
