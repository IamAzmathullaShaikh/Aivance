package com.bangersoul.aivance.feature.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.feature.interview.domain.InterviewFeedback
import com.bangersoul.aivance.feature.interview.domain.InterviewMessage
import com.bangersoul.aivance.feature.interview.domain.InterviewRepository
import com.bangersoul.aivance.feature.interview.domain.MessageRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface InterviewUiState {
    data object Idle : InterviewUiState
    data object Loading : InterviewUiState
    data class Chatting(
        val messages: List<InterviewMessage>,
        val isTyping: Boolean = false
    ) : InterviewUiState
    data object GeneratingFeedback : InterviewUiState
    data class Feedback(val feedback: InterviewFeedback) : InterviewUiState
}

@HiltViewModel
class InterviewViewModel @Inject constructor(
    private val repository: InterviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<InterviewUiState>(InterviewUiState.Idle)
    val uiState: StateFlow<InterviewUiState> = _uiState.asStateFlow()

    fun startInterview(role: String, difficulty: String) {
        viewModelScope.launch {
            repository.startSession(role, difficulty)
                .onStart { _uiState.value = InterviewUiState.Loading }
                .catch { /* Handle error */ }
                .collect { initialMessage ->
                    _uiState.value = InterviewUiState.Chatting(listOf(initialMessage))
                }
        }
    }

    fun sendMessage(text: String) {
        val currentState = _uiState.value
        if (currentState is InterviewUiState.Chatting) {
            val userMessage = InterviewMessage(role = MessageRole.User, text = text)
            val updatedMessages = currentState.messages + userMessage
            _uiState.value = currentState.copy(messages = updatedMessages, isTyping = true)

            viewModelScope.launch {
                repository.sendMessage(text)
                    .catch { _uiState.value = currentState.copy(isTyping = false) }
                    .collect { aiMessage ->
                        _uiState.update { state ->
                            if (state is InterviewUiState.Chatting) {
                                state.copy(
                                    messages = state.messages + aiMessage,
                                    isTyping = false
                                )
                            } else state
                        }
                    }
            }
        }
    }

    fun finishInterview() {
        viewModelScope.launch {
            _uiState.value = InterviewUiState.GeneratingFeedback
            repository.getFeedback()
                .catch { _uiState.value = InterviewUiState.Idle } // Or handle error
                .collect { feedback ->
                    _uiState.value = InterviewUiState.Feedback(feedback)
                }
        }
    }

    fun reset() {
        _uiState.value = InterviewUiState.Idle
    }
}
