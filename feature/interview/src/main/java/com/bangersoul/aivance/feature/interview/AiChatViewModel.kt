package com.bangersoul.aivance.feature.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.enums.MessageRole
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.usecase.ai.ClearConversationUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.RegenerateResponseRequest
import com.bangersoul.aivance.core.domain.usecase.ai.RegenerateResponseUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.SendMessageRequest
import com.bangersoul.aivance.core.domain.usecase.ai.SendMessageUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.StartConversationRequest
import com.bangersoul.aivance.core.domain.usecase.ai.StartConversationUseCase
import com.bangersoul.aivance.core.domain.usecase.ai.SummariseConversationUseCase
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventRequest
import com.bangersoul.aivance.core.domain.usecase.analytics.TrackEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: String = "",
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface AiChatUiState {
    data object Idle : AiChatUiState
    data object Loading : AiChatUiState
    data object Initializing : AiChatUiState
    data class Chatting(
        val messages: List<ChatMessage> = emptyList(),
        val isTyping: Boolean = false,
        val conversationId: String = ""
    ) : AiChatUiState
    data class Error(val message: String) : AiChatUiState
}

sealed interface AiChatUiEvent {
    data class SendMessage(val text: String) : AiChatUiEvent
    data object Regenerate : AiChatUiEvent
    data object Summarise : AiChatUiEvent
    data object ClearConversation : AiChatUiEvent
    data object StartNewChat : AiChatUiEvent
}

sealed interface AiChatUiEffect {
    data class ShowSnackbar(val message: String) : AiChatUiEffect
    data class ShareConversation(val summary: String) : AiChatUiEffect
    data object ScrollToBottom : AiChatUiEffect
}

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val startConversationUseCase: StartConversationUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val regenerateResponseUseCase: RegenerateResponseUseCase,
    private val summariseConversationUseCase: SummariseConversationUseCase,
    private val clearConversationUseCase: ClearConversationUseCase,
    private val trackEventUseCase: TrackEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiChatUiState>(AiChatUiState.Idle)
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private val _effects = Channel<AiChatUiEffect>(Channel.BUFFERED)
    val effects: Flow<AiChatUiEffect> = _effects.receiveAsFlow()

    private var conversationId = ""

    fun onEvent(event: AiChatUiEvent) {
        when (event) {
            is AiChatUiEvent.SendMessage -> sendMessage(event.text)
            AiChatUiEvent.Regenerate -> regenerate()
            AiChatUiEvent.Summarise -> summarise()
            AiChatUiEvent.ClearConversation -> clear()
            AiChatUiEvent.StartNewChat -> startNewChat()
        }
    }

    private fun startNewChat() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "ai_chat_start"))
            _uiState.value = AiChatUiState.Initializing
            conversationId = ""

            val request = StartConversationRequest(
                title = "New Chat",
                providerId = "GEMINI",
                modelName = "gemini-1.5-flash"
            )
            val result = startConversationUseCase(request)
            when (result) {
                is Result.Success -> {
                    conversationId = result.data.id
                    _uiState.value = AiChatUiState.Chatting(conversationId = conversationId)
                }
                is Result.Failure -> {
                    _uiState.value = AiChatUiState.Error(result.error.message ?: "Failed to start conversation")
                }
            }
        }
    }

    private fun sendMessage(text: String) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(role = MessageRole.USER, content = text)
        val currentState = _uiState.value
        val currentMessages = if (currentState is AiChatUiState.Chatting) {
            currentState.messages + userMessage
        } else {
            listOf(userMessage)
        }

        _uiState.value = AiChatUiState.Chatting(
            messages = currentMessages,
            isTyping = true,
            conversationId = conversationId
        )
        sendEffect(AiChatUiEffect.ScrollToBottom)

        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "ai_chat_send"))

            val request = SendMessageRequest(
                conversationId = conversationId,
                message = text
            )
            val result = sendMessageUseCase(request)
            when (result) {
                is Result.Success -> {
                    val aiMessage = ChatMessage(
                        role = MessageRole.ASSISTANT,
                        content = result.data.content
                    )
                    _uiState.value = AiChatUiState.Chatting(
                        messages = currentMessages + aiMessage,
                        isTyping = false,
                        conversationId = conversationId
                    )
                    sendEffect(AiChatUiEffect.ScrollToBottom)
                }
                is Result.Failure -> {
                    _uiState.value = AiChatUiState.Chatting(
                        messages = currentMessages,
                        isTyping = false,
                        conversationId = conversationId
                    )
                    sendEffect(AiChatUiEffect.ShowSnackbar(result.error.message ?: "Failed"))
                }
            }
        }
    }

    private fun regenerate() {
        val currentState = _uiState.value
        if (currentState !is AiChatUiState.Chatting || currentState.messages.isEmpty()) return

        val messagesWithoutLastAi = currentState.messages
            .dropLastWhile { it.role == MessageRole.ASSISTANT }

        _uiState.value = currentState.copy(messages = messagesWithoutLastAi, isTyping = true)

        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "ai_chat_regenerate"))

            val request = RegenerateResponseRequest(conversationId = conversationId)
            val result = regenerateResponseUseCase(request)
            when (result) {
                is Result.Success -> {
                    val aiMessage = ChatMessage(role = MessageRole.ASSISTANT, content = result.data.content)
                    _uiState.value = AiChatUiState.Chatting(
                        messages = messagesWithoutLastAi + aiMessage,
                        isTyping = false,
                        conversationId = conversationId
                    )
                }
                is Result.Failure -> {
                    _uiState.value = currentState.copy(messages = messagesWithoutLastAi, isTyping = false)
                }
            }
        }
    }

    private fun summarise() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "ai_chat_summarise"))
            val result = summariseConversationUseCase(conversationId = conversationId)
            when (result) {
                is Result.Success -> {
                    sendEffect(AiChatUiEffect.ShareConversation(result.data))
                }
                is Result.Failure -> {
                    sendEffect(AiChatUiEffect.ShowSnackbar(result.error.message ?: "Failed"))
                }
            }
        }
    }

    private fun clear() {
        viewModelScope.launch {
            trackEventUseCase(TrackEventRequest(eventName = "ai_chat_clear"))
            clearConversationUseCase(conversationId = conversationId)
            _uiState.value = AiChatUiState.Idle
            conversationId = ""
            sendEffect(AiChatUiEffect.ShowSnackbar("Conversation cleared"))
        }
    }

    private fun sendEffect(effect: AiChatUiEffect) {
        viewModelScope.launch { _effects.send(effect) }
    }
}
