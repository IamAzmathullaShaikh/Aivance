package com.bangersoul.aivance.feature.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AssistantRepository
import com.bangersoul.aivance.core.domain.usecase.assistant.AssistantRequest
import com.bangersoul.aivance.core.domain.usecase.assistant.GetAssistantResponseUseCase
import com.bangersoul.aivance.core.domain.usecase.user.LoadProfileUseCase
import com.bangersoul.aivance.sdk.core.ProviderStatus
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AssistantUiState {
    data object Idle : AssistantUiState
    data object Loading : AssistantUiState

    data class Chatting(
        val messages: List<AssistantChatMessage> = emptyList(),
        val isTyping: Boolean = false,
        /**
         * Non-null while a streaming response is in flight — the partial text
         * accumulated so far. The UI renders it as a live, typewriter bubble
         * until the stream completes and the full message is committed.
         */
        val streamingContent: String? = null,
        /**
         * True when the stream terminated early (network drop, provider error)
         * after some partial text had already arrived. The partial content is
         * kept visible so nothing is lost; the UI stops the blinking caret and
         * offers retry instead of implying generation is still in progress.
         */
        val streamFailed: Boolean = false
    ) : AssistantUiState

    data class Error(val message: String) : AssistantUiState
}

data class AssistantChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/** Compact provider state for the Assistant status bar. */
data class ProviderStatusUi(
    val isReady: Boolean = false,
    val providerName: String? = null,
    val statusLabel: String = "No provider configured"
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val assistantRepository: AssistantRepository,
    private val getAssistantResponseUseCase: GetAssistantResponseUseCase,
    private val providerManager: ProviderManager,
    private val loadProfileUseCase: LoadProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<AssistantUiState>(AssistantUiState.Idle)
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    private val readyStatuses = setOf(
        ProviderStatus.Ready,
        ProviderStatus.Active,
        ProviderStatus.Healthy
    )

    /**
     * Reactive provider bar state: the best currently-ready AI provider's name
     * and status, or an unconfigured prompt when none is available.
     */
    val providerStatus: StateFlow<ProviderStatusUi> = providerManager.providerStatuses
        .map { statuses ->
            val ready = statuses.entries.firstOrNull { (_, status) -> status in readyStatuses }
            if (ready != null) {
                ProviderStatusUi(
                    isReady = true,
                    providerName = friendlyName(ready.key),
                    statusLabel = ready.value.name.replaceFirstChar { it.uppercase() }
                )
            } else {
                ProviderStatusUi(isReady = false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProviderStatusUi())

    /** The user's first name for the personalized greeting header. */
    val userName: StateFlow<String> = loadProfileUseCase.invoke()
        .map { result ->
            (result as? Result.Success)?.data?.fullName
                ?.trim()
                ?.substringBefore(' ')
                .orEmpty()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private var currentConversationId = "main_session"

    private var lastUserMessage: String? = null

    /**
     * Sends a message and streams the reply into the bubble token-by-token.
     *
     * The assistant message appears immediately in "streaming" state and its
     * text grows with every emitted chunk; when the stream completes the full
     * response is persisted and committed as a normal message.
     *
     * Single-flight: a new send is ignored while a stream is still in flight,
     * so rapid sends can never clobber the in-progress bubble's state. If the
     * stream fails after partial text arrived, that partial is persisted and
     * kept visible with [streamFailed] set so the UI can offer retry instead
     * of hanging on a blinking caret forever.
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val current = _uiState.value
        if (current is AssistantUiState.Chatting &&
            (current.isTyping || (current.streamingContent != null && !current.streamFailed))
        ) {
            // A stream is already in flight — ignore the duplicate send.
            return
        }

        lastUserMessage = text

        val userMsg = AssistantChatMessage("USER", text)
        val messages = if (current is AssistantUiState.Chatting) current.messages + userMsg else listOf(userMsg)

        _uiState.value = AssistantUiState.Chatting(messages, isTyping = true)

        viewModelScope.launch {
            assistantRepository.saveMessage(currentConversationId, "USER", text)

            var fullResponse = ""
            try {
                getAssistantResponseUseCase.stream(
                    AssistantRequest(currentConversationId, text)
                ).collect { chunk ->
                    fullResponse += chunk
                    _uiState.value = AssistantUiState.Chatting(
                        messages = messages,
                        isTyping = false,
                        streamingContent = fullResponse
                    )
                }

                if (fullResponse.isBlank()) {
                    _uiState.value = AssistantUiState.Error("AI returned an empty response")
                    return@launch
                }

                // Stream completed — persist and commit the full reply.
                assistantRepository.saveMessage(currentConversationId, "ASSISTANT", fullResponse)
                val aiMsg = AssistantChatMessage("ASSISTANT", fullResponse)
                _uiState.value = AssistantUiState.Chatting(
                    messages = messages + aiMsg,
                    isTyping = false,
                    streamingContent = null
                )
            } catch (e: Exception) {
                if (fullResponse.isNotBlank()) {
                    // Persist whatever arrived so Room history has no dangling
                    // user message, then keep it visible for retry.
                    assistantRepository.saveMessage(currentConversationId, "ASSISTANT", fullResponse)
                    _uiState.value = AssistantUiState.Chatting(
                        messages = messages,
                        isTyping = false,
                        streamingContent = fullResponse,
                        streamFailed = true
                    )
                } else {
                    _uiState.value = AssistantUiState.Error(
                        e.message?.takeIf { it.isNotBlank() } ?: "AI failed to respond"
                    )
                }
            }
        }
    }

    /**
     * Re-sends the last user message after a provider failure.
     */
    fun retry() {
        lastUserMessage?.let { sendMessage(it) }
    }

    private fun friendlyName(providerId: String): String =
        providerId.split('_', '-').filter { it.isNotBlank() }.joinToString(" ") {
            it.replaceFirstChar { c -> c.uppercase() }
        }
}
