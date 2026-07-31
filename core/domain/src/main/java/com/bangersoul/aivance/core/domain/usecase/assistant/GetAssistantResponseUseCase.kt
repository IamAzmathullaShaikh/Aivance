package com.bangersoul.aivance.core.domain.usecase.assistant

import com.bangersoul.aivance.core.common.model.AIMessage
import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.common.result.runCatchingCore
import com.bangersoul.aivance.core.domain.assistant.AssistantContextEngine
import com.bangersoul.aivance.core.domain.assistant.CapabilityRouter
import com.bangersoul.aivance.core.domain.repository.AiRepository
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
 */
class GetAssistantResponseUseCase @Inject constructor(
    private val contextEngine: AssistantContextEngine,
    private val capabilityRouter: CapabilityRouter,
    private val providerManager: ProviderManager,
    private val aiRepository: AiRepository
) : UseCase<AssistantRequest, CoreResult<String>>() {

    override suspend operator fun invoke(input: AssistantRequest): CoreResult<String> = runCatchingCore {
        val platformContext = contextEngine.buildActiveContext()
        val provider = providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider available")

        val systemPrompt = """
            You are the AiVance Career Assistant.
            Your goal is to help the user manage their job applications, resumes, and career growth.

            Current Platform Context:
            $platformContext

            Based on this context, provide proactive, data-driven advice.
            If the user asks to perform an action (e.g., "Analyze my resume"), identify the intent.
        """.trimIndent()

        // Interaction with LLM
        val response = provider.generateText("$systemPrompt\n\nUser: ${input.userMessage}").getOrNull()
            ?: throw Exception("AI failed to respond")

        response
    }
}
