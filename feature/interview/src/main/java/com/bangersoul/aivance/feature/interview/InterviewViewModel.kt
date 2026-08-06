package com.bangersoul.aivance.feature.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.enums.MessageSender
import com.bangersoul.aivance.core.common.model.*
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.domain.engine.CareerStateEngine
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import com.bangersoul.aivance.core.domain.repository.crm.CompanyIntelligenceRepository
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface InterviewUiState {
    data class Idle(
        val history: List<InterviewSession> = emptyList(),
        val careerState: CareerState? = null,
        val readinessScore: Int = 0
    ) : InterviewUiState
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
    data class StartSession(val role: String, val company: String, val type: String, val jobId: Long? = null) : InterviewUiEvent
    data class SubmitAnswer(val text: String) : InterviewUiEvent
    data object NextQuestion : InterviewUiEvent
    data object Complete : InterviewUiEvent
    data object Reset : InterviewUiEvent
    data object LoadHistory : InterviewUiEvent
}

@HiltViewModel
class InterviewViewModel @Inject constructor(
    private val interviewRepository: InterviewRepository,
    private val careerStateEngine: CareerStateEngine,
    private val companyRepository: CompanyIntelligenceRepository,
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
        observeCareerState()
    }

    private fun observeCareerState() {
        viewModelScope.launch {
            careerStateEngine.state.collect { state ->
                val current = _uiState.value
                if (current is InterviewUiState.Idle) {
                    _uiState.value = current.copy(
                        careerState = state,
                        readinessScore = calculateReadiness(state, current.history)
                    )
                }
            }
        }
    }

    private fun calculateReadiness(state: CareerState, history: List<InterviewSession>): Int {
        val lastScore = history.firstOrNull { it.isCompleted }?.feedback?.overallScore ?: 0
        return (lastScore + (state.growth.careerScore / 10)).coerceIn(0, 100)
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

            // Find the correct resume version for this job if possible
            val resumeVersionId = event.jobId?.let { jid ->
                // Logic to find best resume for this job context
                null
            }

            val result = interviewRepository.startSession(
                role = event.role,
                company = event.company,
                difficulty = InterviewDifficulty.MEDIUM,
                jobId = event.jobId,
                resumeVersionId = resumeVersionId,
                type = event.type
            )

            if (result is Result.Success) {
                val session = result.data
                _uiState.value = InterviewUiState.Active(session = session)
                // Generate questions, then refresh the session so the screen shows them.
                val questionsResult = interviewRepository.generateQuestions(session.id, 5)
                val generated = if (questionsResult is Result.Success) {
                    interviewRepository.getQuestions(session.id).firstOrNull()?.getOrNull().orEmpty()
                } else {
                    emptyList()
                }
                val questions = generated.ifEmpty {
                    // No AI provider configured (or generation failed): seed the
                    // session with a role-specific STAR prep pack (R-05) so the
                    // user can still practice and the screen never dead-ends on
                    // an eternal "Preparing…" state. Note: these in-memory
                    // questions are not persisted to InterviewDao (they have no
                    // session row), so a later session reload won't replay them.
                    android.util.Log.w(
                        "InterviewViewModel",
                        "AI question generation unavailable — using STAR prep fallback"
                    )
                    STARPrepGenerator.generateStarPack(event.role)
                }
                _uiState.value = InterviewUiState.Active(session = session.copy(questions = questions))
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
