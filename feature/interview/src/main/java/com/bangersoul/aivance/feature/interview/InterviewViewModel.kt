package com.bangersoul.aivance.feature.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.model.InterviewMessage
import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface InterviewUiState {
    data object Idle : InterviewUiState
    data object Preparing : InterviewUiState
    data class Active(
        val session: InterviewSession,
        val currentQuestionIndex: Int = 0,
        val isSubmitting: Boolean = false
    ) : InterviewUiState
    data class Review(val session: InterviewSession) : InterviewUiState
    data class Error(val message: String) : InterviewUiState
}

sealed interface InterviewUiEvent {
    data class StartSession(val role: String, val company: String, val type: String) : InterviewUiEvent
    data class SubmitAnswer(val text: String) : InterviewUiEvent
    data object NextQuestion : InterviewUiEvent
    data object Complete : InterviewUiEvent
    data object Reset : InterviewUiEvent
    data object LoadHistory : InterviewUiEvent
}

@HiltViewModel
class InterviewViewModel @Inject constructor(
    private val interviewRepository: InterviewRepository,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InterviewUiState>(InterviewUiState.Idle)
    val uiState: StateFlow<InterviewUiState> = _uiState.asStateFlow()

    private val _effects = Channel<Unit>(Channel.BUFFERED)
    val effects: Flow<Unit> = _effects.receiveAsFlow()

    fun onEvent(event: InterviewUiEvent) {
        when (event) {
            is InterviewUiEvent.StartSession -> startSession(event)
            is InterviewUiEvent.SubmitAnswer -> submitAnswer(event.text)
            InterviewUiEvent.NextQuestion -> nextQuestion()
            InterviewUiEvent.Complete -> completeSession()
            InterviewUiEvent.Reset -> reset()
            InterviewUiEvent.LoadHistory -> { /* TODO */ }
        }
    }

    private fun startSession(event: InterviewUiEvent.StartSession) {
        viewModelScope.launch {
            _uiState.value = InterviewUiState.Preparing
            trackEventUseCase(TrackEventRequest("interview_session_start"))

            val result = interviewRepository.startSession(
                role = event.role,
                company = event.company,
                difficulty = InterviewDifficulty.MEDIUM,
                jobId = null,
                resumeVersionId = null,
                type = event.type
            )

            if (result is Result.Success) {
                // Generate initial questions
                interviewRepository.generateQuestions(result.data.id, 5)
                _uiState.value = InterviewUiState.Active(session = result.data)
            } else {
                _uiState.value = InterviewUiState.Error("Failed to start session")
            }
        }
    }

    private fun submitAnswer(answerText: String) {
        val current = _uiState.value as? InterviewUiState.Active ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isSubmitting = true)

            val message = InterviewMessage(
                id = "0",
                sessionId = current.session.id,
                sender = com.bangersoul.aivance.core.common.enums.MessageSender.USER,
                text = answerText
            )

            val result = interviewRepository.submitAnswer(current.session.id, message)
            if (result is Result.Success) {
                _uiState.value = current.copy(isSubmitting = false)
                // Move to next question or evaluation
            } else {
                _uiState.value = current.copy(isSubmitting = false)
            }
        }
    }

    private fun nextQuestion() {
        val current = _uiState.value as? InterviewUiState.Active ?: return
        _uiState.value = current.copy(currentQuestionIndex = current.currentQuestionIndex + 1)
    }

    private fun completeSession() {
        val current = _uiState.value as? InterviewUiState.Active ?: return
        viewModelScope.launch {
            interviewRepository.completeSession(current.session.id)
            _uiState.value = InterviewUiState.Review(current.session)
        }
    }

    private fun reset() {
        _uiState.value = InterviewUiState.Idle
    }
}
