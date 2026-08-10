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

        var fullResponse = ""
        var primaryProvider: AIProvider? = null

        // Prefer a streaming-capable provider for real-time token delivery.
        val streamingProvider =
            providerManager.getBestProviderFor(ProviderCapability.AI.Streaming) as? AIProvider

        if (streamingProvider != null) {
            primaryProvider = streamingProvider
            fullResponse = streamChat(streamingProvider, sdkMessages) { chunk -> emit(chunk) }
        } else {
            // Non-streaming provider emits the full answer once.
            val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            primaryProvider = provider
            fullResponse = provider?.chat(sdkMessages)?.getOrNull().orEmpty()
            if (fullResponse.isNotBlank()) emit(fullResponse)
        }

        // Zero-connectivity fallback: the on-device model (Gemma) works without
        // any network once its model file is downloaded. Try it before giving up
        // on a canned Copilot reply, so the Assistant stays useful offline and
        // when the configured cloud provider is unreachable.
        if (fullResponse.isBlank()) {
            val onDeviceProvider =
                providerManager.getOnDeviceProviderFor(ProviderCapability.AI.Streaming) as? AIProvider
                    ?: providerManager.getOnDeviceProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            // Guard against re-trying the same instance (e.g. when the on-device
            // model is already the best configured provider).
            if (onDeviceProvider != null && onDeviceProvider !== primaryProvider) {
                fullResponse = streamChat(onDeviceProvider, sdkMessages) { chunk -> emit(chunk) }
            }
        }

        if (fullResponse.isBlank()) {
            emit(generateCopilotFallback(input.userMessage, platformContext))
        }
    }

    /**
     * Streams a chat response from [provider], emitting each chunk via [emit]
     * and returning the accumulated full text. Provider failures (exceptions,
     * per-chunk [Result.Failure]) are swallowed so callers can fall back.
     */
    private suspend fun streamChat(
        provider: AIProvider,
        messages: List<SdkAiMessage>,
        emit: suspend (String) -> Unit
    ): String {
        var fullResponse = ""
        try {
            provider.streamChat(messages).collect { chunkResult ->
                when (chunkResult) {
                    is Result.Success -> {
                        emit(chunkResult.data)
                        fullResponse += chunkResult.data
                    }
                    is Result.Failure -> {}
                }
            }
        } catch (_: Exception) {}
        return fullResponse
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
        val systemPrompt = buildSystemPrompt(platformContext)
        val prompt = "$systemPrompt\n\nUser: $userMessage"

        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
        val onDeviceProvider =
            providerManager.getOnDeviceProviderFor(ProviderCapability.AI.Chat) as? AIProvider
        val response = provider?.generateText(prompt)?.getOrNull()

        // Zero-connectivity fallback: the on-device model (Gemma) works without
        // any network once its model file is downloaded. Guard against re-trying
        // the same instance (when the on-device model is already the best chat
        // provider).
        if (response.isNullOrBlank() && onDeviceProvider != null && onDeviceProvider !== provider) {
            onDeviceProvider.generateText(prompt)?.getOrNull()?.let { return it }
        }

        if (!response.isNullOrBlank()) return response

        // No provider produced an answer: surface a clear error only when there
        // is truly nothing to route to (no cloud provider and no on-device model).
        if (provider == null && onDeviceProvider == null) {
            throw Exception("No AI provider configured — open Settings → Providers to connect one.")
        }
        return generateCopilotFallback(userMessage, platformContext)
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

    private fun generateCopilotFallback(message: String, platformContext: String): String {
        val lower = message.lowercase()
        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> {
                "Hello! I am your AiVance Copilot. I am actively monitoring your career workspace.\n\n" +
                "How can I assist you today? You can ask me to search jobs, analyze your resume, prepare for interviews, or optimize your applications!"
            }
            lower.contains("resume") || lower.contains("cv") || lower.contains("ats") -> {
                "Here is guidance based on your active resume context:\n\n" +
                "• Highlight top core technical competencies in your summary section.\n" +
                "• Quantify your achievements (e.g., 'Reduced response latency by 40%').\n" +
                "• Run an ATS Scan in the Resume Engine to tailor your resume for specific positions."
            }
            lower.contains("job") || lower.contains("search") || lower.contains("apply") -> {
                "Here are strategic recommendations for your job search:\n\n" +
                "1. Filter jobs in the Discovery tab by location and workplace preference (Remote, Hybrid, On-site).\n" +
                "2. Maintain active applications in your Pipeline Tracker.\n" +
                "3. Use the Prep Studio to practice mock questions for upcoming interview rounds."
            }
            else -> {
                "I've evaluated your career profile:\n\n" +
                "1. Tailor your resume for target applications using the Resume Engine.\n" +
                "2. Practice interactive mock interviews in the Prep Studio.\n" +
                "3. Track your active applications and scheduled interviews in the Pipeline board."
            }
        }
    }
}
