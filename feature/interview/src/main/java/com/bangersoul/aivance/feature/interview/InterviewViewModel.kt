package com.bangersoul.aivance.feature.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.EndInterviewUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.EvaluateAnswersRequest
import com.bangersoul.aivance.core.domain.usecase.interview.EvaluateAnswersUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.GenerateFeedbackUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.GenerateInterviewQuestionsUseCase
import com.bangersoul.aivance.core.domain.usecase.interview.StartInterviewSessionRequest
import com.bangersoul.aivance.core.domain.usecase.interview.StartInterviewSessionUseCase
import com.bangersoul.aivance.feature.interview.domain.InterviewFeedback
import com.bangersoul.aivance.feature.interview.domain.InterviewMessage
import com.bangersoul.aivance.feature.interview.domain.MessageRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface InterviewUiState {
    data object Idle : InterviewUiState
    data object Preparing : InterviewUiState
    data class Ready(
        val sessionId: String = "",
        val role: String = "",
        val difficulty: String = "MEDIUM"
    ) : InterviewUiState
    data object Loading : InterviewUiState
    data class Chatting(
        val messages: List<InterviewMessage> = emptyList(),
        val isTyping: Boolean = false,
        val sessionId: String = ""
    ) : InterviewUiState
    data object GeneratingFeedback : InterviewUiState
    data class Feedback(
        val feedback: InterviewFeedback,
        val sessionId: String = ""
    ) : InterviewUiState
    data class Error(val message: String) : InterviewUiState
}

sealed interface InterviewUiEvent {
    data class StartSession(val role: String, val difficulty: InterviewDifficulty = InterviewDifficulty.MEDIUM) : InterviewUiEvent
    data class SendMessage(val text: String) : InterviewUiEvent
    data class EndSession(val sessionId: String) : InterviewUiEvent
    data object GenerateQuestions : InterviewUiEvent
    data object Reset : InterviewUiEvent
}

sealed interface InterviewUiEffect {
    data class ShowSnackbar(val message: String) : InterviewUiEffect
    data class NavigateToHistory(val sessionId: String) : InterviewUiEffect
    data object ScrollToBottom : InterviewUiEffect
}

@HiltViewModel
class InterviewViewModel @Inject constructor(
    private val startInterviewSessionUseCase: StartInterviewSessionUseCase,
    private val generateInterviewQuestionsUseCase: GenerateInterviewQuestionsUseCase,
    private val evaluateAnswersUseCase: EvaluateAnswersUseCase,
    private val generateFeedbackUseCase: GenerateFeedbackUseCase,
    private val endInterviewUseCase: EndInterviewUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<InterviewUiState>(InterviewUiState.Idle)
    val uiState: StateFlow<InterviewUiState> = _uiState.asStateFlow()

    private val _effects = Channel<InterviewUiEffect>(Channel.BUFFERED)
    val effects: Flow<InterviewUiEffect> = _effects.receiveAsFlow()

    private var currentQuestion = "Tell me about yourself."

    fun onEvent(event: InterviewUiEvent) {
        when (event) {
            is InterviewUiEvent.StartSession -> startSession(event.role, event.difficulty)
            is InterviewUiEvent.SendMessage -> sendMessage(event.text)
            is InterviewUiEvent.EndSession -> endSession(event.sessionId)
            InterviewUiEvent.GenerateQuestions -> generateQuestions()
            InterviewUiEvent.Reset -> reset()
        }
    }

    private fun startSession(role: String, difficulty: InterviewDifficulty) {
        if (role.isBlank()) {
            _uiState.value = InterviewUiState.Error("Role cannot be empty")
            return
        }
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "interview_start"))
            _uiState.value = InterviewUiState.Preparing

            val request = StartInterviewSessionRequest(
                targetRole = role,
                difficulty = difficulty
            )
            val result = startInterviewSessionUseCase(request)
            when (result) {
                is CoreResult.Success -> {
                    val session = result.data
                    _uiState.value = InterviewUiState.Ready(
                        sessionId = session.id,
                        role = role,
                        difficulty = difficulty.name
                    )
                    sendEffect(InterviewUiEffect.ScrollToBottom)
                }
                is CoreResult.Failure -> {
                    _uiState.value = InterviewUiState.Error(result.error.message ?: "Failed to start")
                }
            }
        }
    }

    private fun sendMessage(text: String) {
        if (text.isBlank()) return
        val currentState = _uiState.value
        if (currentState is InterviewUiState.Ready || currentState is InterviewUiState.Chatting) {
            val sessionId = when (currentState) {
                is InterviewUiState.Ready -> currentState.sessionId
                is InterviewUiState.Chatting -> currentState.sessionId
                else -> return
            }
            val userMessage = InterviewMessage(role = MessageRole.User, text = text)
            val updatedMessages = if (currentState is InterviewUiState.Chatting) {
                currentState.messages + userMessage
            } else {
                listOf(userMessage)
            }
            _uiState.value = InterviewUiState.Chatting(
                messages = updatedMessages, isTyping = true, sessionId = sessionId
            )
            sendEffect(InterviewUiEffect.ScrollToBottom)

            viewModelScope.launch {
                val request = EvaluateAnswersRequest(
                    sessionId = sessionId,
                    question = currentQuestion,
                    answer = text
                )
                val result = evaluateAnswersUseCase(request)
                when (result) {
                    is CoreResult.Success -> {
                        val aiMessage = InterviewMessage(role = MessageRole.AI, text = result.data.feedback)
                        currentQuestion = result.data.feedback.take(100)
                        _uiState.update { state ->
                            if (state is InterviewUiState.Chatting) {
                                state.copy(messages = state.messages + aiMessage, isTyping = false)
                            } else state
                        }
                        sendEffect(InterviewUiEffect.ScrollToBottom)
                    }
                    is CoreResult.Failure -> {
                        _uiState.update { state ->
                            if (state is InterviewUiState.Chatting) state.copy(isTyping = false) else state
                        }
                        sendEffect(InterviewUiEffect.ShowSnackbar("Failed to get response"))
                    }
                }
            }
        }
    }

    private fun generateQuestions() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "interview_generate_questions"))
            generateInterviewQuestionsUseCase("", "")
                .catch { e -> sendEffect(InterviewUiEffect.ShowSnackbar(e.message ?: "Failed")) }
                .collect { result ->
                    when (result) {
                        is CoreResult.Success -> {
                            val msg = InterviewMessage(role = MessageRole.AI, text = result.data)
                            _uiState.update { state ->
                                if (state is InterviewUiState.Chatting) {
                                    state.copy(messages = state.messages + msg)
                                } else state
                            }
                        }
                        is CoreResult.Failure -> {
                            sendEffect(InterviewUiEffect.ShowSnackbar(result.error.message ?: "Failed"))
                        }
                    }
                }
        }
    }

    private fun endSession(sessionId: String) {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "interview_end"))
            _uiState.value = InterviewUiState.GeneratingFeedback

            val result = endInterviewUseCase(sessionId = sessionId)
            when (result) {
                is CoreResult.Success -> {
                    _uiState.value = InterviewUiState.Feedback(
                        feedback = InterviewFeedback(
                            summary = "Interview completed successfully",
                            strengths = listOf("Completed all questions"),
                            weaknesses = emptyList(),
                            tips = listOf("Review answers and try again")
                        ),
                        sessionId = sessionId
                    )
                }
                is CoreResult.Failure -> {
                    _uiState.value = InterviewUiState.Error(result.error.message ?: "Failed")
                }
            }
        }
    }

    private fun reset() { _uiState.value = InterviewUiState.Idle }

    private fun sendEffect(effect: InterviewUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
