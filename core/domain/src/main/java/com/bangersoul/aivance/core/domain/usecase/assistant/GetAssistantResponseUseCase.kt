package com.bangersoul.aivance.core.domain.usecase.assistant

import com.bangersoul.aivance.core.common.model.AIMessage
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
 */
class GetAssistantResponseUseCase @Inject constructor(
    private val contextEngine: AssistantContextEngine,
    private val capabilityRouter: CapabilityRouter,
    private val providerManager: ProviderManager
) : UseCase<AssistantRequest, CoreResult<String>>() {

    override suspend operator fun invoke(input: AssistantRequest): CoreResult<String> = runCatchingCore {
        // Stage 1: detect + execute concrete intents before falling back to chat.
        val intent = detectIntent(input.userMessage)
        if (intent != null) {
            val routed = capabilityRouter.routeIntent(intent.first, intent.second)
            val routedText = routed.getOrNull()
            if (routedText != null) {
                return@runCatchingCore routedText
            }
        }

        // Stage 2: context-aware LLM chat with the active provider.
        val platformContext = contextEngine.buildActiveContext()
        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider configured — open Settings → Providers to connect one.")

        val systemPrompt = """
            You are the AiVance Career Assistant.
            Your goal is to help the user manage their job applications, resumes, and career growth.

            Current Platform Context:
            $platformContext

            Based on this context, provide proactive, data-driven advice.
            If the user asks to perform an action (e.g., "Analyze my resume"), identify the intent.
        """.trimIndent()

        val response = provider.generateText("$systemPrompt\n\nUser: ${input.userMessage}").getOrNull()
            ?: throw Exception("AI failed to respond")

        response
    }

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
