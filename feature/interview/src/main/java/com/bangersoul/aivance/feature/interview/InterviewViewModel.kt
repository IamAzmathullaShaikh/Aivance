package com.bangersoul.aivance.feature.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.enums.MessageSender
import com.bangersoul.aivance.core.common.model.InterviewMessage
import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface InterviewUiState {
    data class Idle(val history: List<InterviewSession> = emptyList()) : InterviewUiState
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

    private val _uiState = MutableStateFlow<InterviewUiState>(InterviewUiState.Idle())
    val uiState: StateFlow<InterviewUiState> = _uiState.asStateFlow()

    private val _effects = Channel<Unit>(Channel.BUFFERED)
    val effects: Flow<Unit> = _effects.receiveAsFlow()

    private var messageCounter = 0L
    private var historyJob: kotlinx.coroutines.Job? = null

    init {
        loadHistory()
    }

    fun onEvent(event: InterviewUiEvent) {
        when (event) {
            is InterviewUiEvent.StartSession -> startSession(event)
            is InterviewUiEvent.SubmitAnswer -> submitAnswer(event.text)
            InterviewUiEvent.NextQuestion -> nextQuestion()
            InterviewUiEvent.Complete -> completeSession()
            InterviewUiEvent.Reset -> reset()
            InterviewUiEvent.LoadHistory -> loadHistory()
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
                val session = result.data
                _uiState.value = InterviewUiState.Active(session = session)
                // Generate questions, then refresh the session so the screen shows them.
                val questionsResult = interviewRepository.generateQuestions(session.id, 5)
                if (questionsResult is Result.Success) {
                    val withQuestions = interviewRepository.getQuestions(session.id).firstOrNull()?.getOrNull()
                    val refreshed = withQuestions?.let { session.copy(questions = it) } ?: session
                    _uiState.value = InterviewUiState.Active(session = refreshed)
                } else {
                    // Surface the real cause instead of leaving an eternal "Preparing…" state.
                    _uiState.value = InterviewUiState.Error(
                        (questionsResult as? Result.Failure)?.error?.message ?: "Failed to generate questions"
                    )
                }
            } else {
                _uiState.value = InterviewUiState.Error(
                    (result as? Result.Failure)?.error?.message ?: "Failed to start session"
                )
            }
        }
    }

    private fun submitAnswer(answerText: String) {
        val current = _uiState.value as? InterviewUiState.Active ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(isSubmitting = true)

            val messageId = "user-${System.currentTimeMillis()}-${messageCounter++}"
            val message = InterviewMessage(
                id = messageId,
                sessionId = current.session.id,
                sender = MessageSender.USER,
                text = answerText
            )

            val result = interviewRepository.submitAnswer(current.session.id, message)
            if (result is Result.Success) {
                _uiState.value = current.copy(isSubmitting = false)
            } else {
                // Surface the real cause instead of silently swallowing the failure.
                _uiState.value = InterviewUiState.Error(
                    "Failed to submit answer: ${(result as? Result.Failure)?.error?.message ?: "Unknown error"}"
                )
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
            trackEventUseCase(TrackEventRequest("interview_session_complete"))
            interviewRepository.completeSession(current.session.id)
            // Reload the session to pick up the AI feedback generated on completion.
            val completed = interviewRepository.getSessionById(current.session.id).firstOrNull()?.getOrNull()
            val reviewSession = completed ?: current.session.copy(isCompleted = true)
            _uiState.value = InterviewUiState.Review(reviewSession)
            loadHistory()
        }
    }

    private fun reset() {
        _uiState.value = InterviewUiState.Idle()
        loadHistory()
    }

    private fun loadHistory() {
        // Keep a single subscription to the history flow; cancel any previous
        // collector so repeated calls don't accumulate live Room observers.
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            interviewRepository.getSessions()
                .catch { emit(Result.Failure(com.bangersoul.aivance.core.common.result.DomainError(it.message ?: "Failed to load history"))) }
                .collect { result ->
                    val sessions = result.getOrNull()
                    if (sessions != null) {
                        val current = _uiState.value
                        if (current is InterviewUiState.Idle) {
                            _uiState.value = current.copy(history = sessions)
                        }
                    }
                }
        }
    }
}
