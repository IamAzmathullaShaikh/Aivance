package com.bangersoul.aivance.feature.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bangersoul.aivance.core.common.model.AssistantJobContext
import com.bangersoul.aivance.core.domain.engine.CareerStateEngine
import com.bangersoul.aivance.core.domain.engine.ContextEngine
import com.bangersoul.aivance.core.domain.engine.IntentEngine
import com.bangersoul.aivance.core.domain.engine.PromptOrchestrator
import com.bangersoul.aivance.core.domain.repository.AssistantRepository
import com.bangersoul.aivance.core.domain.usecase.assistant.AssistantRequest
import com.bangersoul.aivance.core.domain.usecase.assistant.GetAssistantResponseUseCase
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
        val streamingContent: String? = null,
        val streamFailed: Boolean = false
    ) : AssistantUiState

    data class Error(val message: String) : AssistantUiState
}

data class AssistantChatMessage(
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

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
    private val stateEngine: CareerStateEngine,
    private val contextEngine: ContextEngine,
    private val intentEngine: IntentEngine,
    private val promptOrchestrator: PromptOrchestrator
) : ViewModel() {

    private val _uiState = MutableStateFlow<AssistantUiState>(AssistantUiState.Idle)
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    /** The full career state for the Copilot workspace. */
    val careerState: StateFlow<com.bangersoul.aivance.core.common.model.CareerState> = stateEngine.state

    private val readyStatuses = setOf(
        ProviderStatus.Ready,
        ProviderStatus.Active,
        ProviderStatus.Healthy
    )

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

    val userName: StateFlow<String> = stateEngine.state
        .map { it.profile.name.substringBefore(' ') }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private var currentConversationId = "main_session"
    private var lastUserMessage: String? = null

    /**
     * Job the user is currently looking at (surfaced from saved jobs / job
     * details via the global assistant overlay). Included in the next prompt so
     * answers are tailored to that role.
     */
    private val _jobContext = MutableStateFlow<AssistantJobContext?>(null)
    val jobContext: StateFlow<AssistantJobContext?> = _jobContext.asStateFlow()

    fun setJobContext(context: AssistantJobContext?) {
        _jobContext.value = context
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val current = _uiState.value
        if (current is AssistantUiState.Chatting &&
            (current.isTyping || (current.streamingContent != null && !current.streamFailed))
        ) {
            return
        }

        lastUserMessage = text
        val userMsg = AssistantChatMessage("USER", text)
        val messages = if (current is AssistantUiState.Chatting) current.messages + userMsg else listOf(userMsg)

        _uiState.value = AssistantUiState.Chatting(messages, isTyping = true)

        viewModelScope.launch {
            assistantRepository.saveMessage(currentConversationId, "USER", text)

            val state = stateEngine.state.value
            val intent = intentEngine.detectIntent(text, state)
            val orchestratedPrompt = promptOrchestrator.buildCopilotPrompt(
                text,
                state,
                intent,
                jobContext = _jobContext.value
            )

            var fullResponse = ""
            try {
                getAssistantResponseUseCase.stream(
                    AssistantRequest(currentConversationId, orchestratedPrompt)
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

                assistantRepository.saveMessage(currentConversationId, "ASSISTANT", fullResponse)
                val aiMsg = AssistantChatMessage("ASSISTANT", fullResponse)
                _uiState.value = AssistantUiState.Chatting(
                    messages = messages + aiMsg,
                    isTyping = false,
                    streamingContent = null
                )
            } catch (e: Exception) {
                if (fullResponse.isNotBlank()) {
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

    fun retry() {
        lastUserMessage?.let { sendMessage(it) }
    }

    private fun friendlyName(providerId: String): String =
        providerId.split('_', '-').filter { it.isNotBlank() }.joinToString(" ") {
            it.replaceFirstChar { c -> c.uppercase() }
        }
}
