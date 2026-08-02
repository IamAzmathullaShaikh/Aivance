package com.bangersoul.aivance.core.domain.usecase.assistant

import com.bangersoul.aivance.core.common.enums.MessageRole
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.assistant.AssistantContextEngine
import com.bangersoul.aivance.core.domain.assistant.CapabilityRouter
import com.bangersoul.aivance.core.domain.usecase.UseCase
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import com.bangersoul.aivance.sdk.model.AiMessage as SdkAiMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class AssistantRequest(
    val conversationId: String,
    val userMessage: String
)

/**
 * Orchestrates the AI Assistant response generation.
 *
 * Two-stage pipeline:
 *  1. Intent detection + capability routing — so the Assistant can execute
 *     platform workflows (resume analysis, job search, roadmap) directly.
 *  2. Context-aware LLM fallback for open-ended conversation.
 *
 * Two entry points:
 *  - [invoke] returns the full response as a single [CoreResult].
 *  - [stream] emits token chunks in real time (when the active provider
 *    supports [ProviderCapability.AI.Streaming]), falling back to a single
 *    emission for providers that only support one-shot generation.
 */
class GetAssistantResponseUseCase @Inject constructor(
    private val contextEngine: AssistantContextEngine,
    private val capabilityRouter: CapabilityRouter,
    private val providerManager: ProviderManager
) : UseCase<AssistantRequest, CoreResult<String>>() {

    override suspend operator fun invoke(input: AssistantRequest): CoreResult<String> = runCatchingCore {
        generateResponse(input.userMessage)
    }

    /**
     * Streaming variant. Every emitted string is a partial token chunk; the
     * stream completes when the full response has been generated. Routed
     * intents (resume analysis, job search, roadmap, interview) execute
     * synchronously and emit their result as a single chunk.
     *
     * Falls back gracefully to one-shot generation when no streaming-capable
     * provider is configured, so the Assistant never breaks on older setups.
     */
    fun stream(input: AssistantRequest): Flow<String> = flow {
        // Stage 1: detect + execute concrete intents before falling back to chat.
        routeOrNull(input.userMessage)?.let {
            emit(it)
            return@flow
        }

        // Stage 2: context-aware LLM chat with the active provider.
        val platformContext = contextEngine.buildActiveContext()
        val systemPrompt = buildSystemPrompt(platformContext)

        val sdkMessages = listOf(
            SdkAiMessage(MessageRole.SYSTEM, systemPrompt),
            SdkAiMessage(MessageRole.USER, input.userMessage)
        )

        // Prefer a streaming-capable provider for real-time token delivery.
        val streamingProvider =
            providerManager.getBestProviderFor(ProviderCapability.AI.Streaming) as? AIProvider

        if (streamingProvider != null) {
            var fullResponse = ""
            streamingProvider.streamChat(sdkMessages).collect { chunkResult ->
                when (chunkResult) {
                    is Result.Success -> {
                        emit(chunkResult.data)
                        fullResponse += chunkResult.data
                    }
                    is Result.Failure -> throw Exception(chunkResult.error.message)
                }
            }
            // Guard: some providers emit only a terminal [DONE] with no body.
            if (fullResponse.isBlank()) {
                throw Exception("AI returned an empty response")
            }
        } else {
            // Graceful fallback: non-streaming provider emits one-shot text.
            val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
                ?: throw Exception("No AI provider configured — open Settings → Providers to connect one.")
            val response = provider.chat(sdkMessages).getOrNull()
                ?: throw Exception("AI failed to respond")
            emit(response)
        }
    }

    /**
     * Runs the full generation pipeline for one-shot (non-streaming) callers.
     * Routed intents short-circuit; everything else goes to the context-aware
     * LLM chat path.
     */
    private suspend fun generateResponse(userMessage: String): String {
        // Stage 1: detect + execute concrete intents before falling back to chat.
        routeOrNull(userMessage)?.let { return it }

        // Stage 2: context-aware LLM chat with the active provider.
        val platformContext = contextEngine.buildActiveContext()
        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider configured — open Settings → Providers to connect one.")

        val systemPrompt = buildSystemPrompt(platformContext)
        return provider.generateText("$systemPrompt\n\nUser: $userMessage").getOrNull()
            ?: throw Exception("AI failed to respond")
    }

    /**
     * Runs Stage 1 of the pipeline: intent detection + capability routing.
     * Returns the routed result when the intent was executable, null when the
     * message should fall back to the context-aware LLM chat path.
     */
    private suspend fun routeOrNull(message: String): String? {
        val intent = detectIntent(message) ?: return null
        return capabilityRouter.routeIntent(intent.first, intent.second).getOrNull()
    }

    private fun buildSystemPrompt(platformContext: String): String = """
            You are the AiVance Career Assistant.
            Your goal is to help the user manage their job applications, resumes, and career growth.

            Current Platform Context:
            $platformContext

            Based on this context, provide proactive, data-driven advice.
            If the user asks to perform an action (e.g., "Analyze my resume"), identify the intent.
        """.trimIndent()

    /**
     * Maps a free-form user message to a (intent, params) pair using keyword
     * detection. Returns null when the message should fall back to chat.
     */
    private fun detectIntent(message: String): Pair<String, Map<String, String>>? {
        val lower = message.lowercase().trim()
        return when {
            (lower.contains("resume") && (lower.contains("analyz") || lower.contains("score") || lower.contains("optimiz"))) ||
                lower.contains("ats") -> {
                val jd = extractAfter(message, listOf("against", "for", "with"))
                "ANALYZE_RESUME" to mapOf("jobDescription" to jd)
            }
            (lower.contains("job") || lower.contains("role") || lower.contains("position")) &&
                (lower.contains("search") || lower.contains("find") || lower.contains("look for") || lower.contains("match")) -> {
                val query = extractAfter(message, listOf("for", "as", "matching"))
                "SEARCH_JOBS" to mapOf("query" to query.ifBlank { message })
            }
            lower.contains("roadmap") || lower.contains("career path") || lower.contains("plan my career") -> {
                val role = extractAfter(message, listOf("to", "toward", "for"))
                "GENERATE_ROADMAP" to mapOf("targetRole" to role)
            }
            lower.contains("interview") && (lower.contains("mock") || lower.contains("practice") || lower.contains("prepare")) -> {
                val role = extractAfter(message, listOf("for", "as"))
                "START_INTERVIEW" to mapOf("targetRole" to role)
            }
            else -> null
        }
    }

    private fun extractAfter(message: String, markers: List<String>): String {
        val lower = message.lowercase()
        markers.forEach { marker ->
            val idx = lower.indexOf(marker)
            if (idx >= 0) {
                val rest = message.substring(idx + marker.length).trim()
                if (rest.isNotBlank()) return rest.trimEnd('?', '.', '!')
            }
        }
        return ""
    }
}
