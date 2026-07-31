package com.bangersoul.aivance.feature.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AssistantConversation
import com.bangersoul.aivance.core.domain.repository.AssistantRepository
import com.bangersoul.aivance.core.domain.usecase.assistant.AssistantRequest
import com.bangersoul.aivance.core.domain.usecase.assistant.GetAssistantResponseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AssistantUiState {
    data object Idle : AssistantUiState
    data object Loading : AssistantUiState
    data class Chatting(
        val messages: List<AssistantChatMessage> = emptyList(),
        val isTyping: Boolean = false
    ) : AssistantUiState
    data class Error(val message: String) : AssistantUiState
}

data class AssistantChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val assistantRepository: AssistantRepository,
    private val getAssistantResponseUseCase: GetAssistantResponseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AssistantUiState>(AssistantUiState.Idle)
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private var currentConversationId = "main_session"

    private var lastUserMessage: String? = null

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        lastUserMessage = text

        val userMsg = AssistantChatMessage("USER", text)
        val current = _uiState.value
        val messages = if (current is AssistantUiState.Chatting) current.messages + userMsg else listOf(userMsg)

        _uiState.value = AssistantUiState.Chatting(messages, isTyping = true)

        viewModelScope.launch {
            assistantRepository.saveMessage(currentConversationId, "USER", text)

            val result = getAssistantResponseUseCase(AssistantRequest(currentConversationId, text))
            if (result is Result.Success) {
                val aiMsg = AssistantChatMessage("ASSISTANT", result.data)
                assistantRepository.saveMessage(currentConversationId, "ASSISTANT", result.data)
                _uiState.value = AssistantUiState.Chatting(messages + aiMsg, isTyping = false)
            } else {
                _uiState.value = AssistantUiState.Error("AI failed to respond")
            }
        }
    }

    /**
     * Re-sends the last user message after a provider failure.
     */
    fun retry() {
        lastUserMessage?.let { sendMessage(it) }
    }
}
