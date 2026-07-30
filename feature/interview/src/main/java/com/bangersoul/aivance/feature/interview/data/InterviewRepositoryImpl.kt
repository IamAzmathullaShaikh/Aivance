package com.bangersoul.aivance.feature.interview.data

import com.bangersoul.aivance.feature.interview.domain.InterviewFeedback
import com.bangersoul.aivance.feature.interview.domain.InterviewMessage
import com.bangersoul.aivance.feature.interview.domain.InterviewRepository
import com.bangersoul.aivance.feature.interview.domain.MessageRole
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import com.bangersoul.aivance.sdk.model.AiMessage as SdkAiMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import com.bangersoul.aivance.core.common.enums.MessageRole as CommonMessageRole
import com.bangersoul.aivance.core.common.result.getOrElse
import com.bangersoul.aivance.core.common.result.getOrNull

@Singleton
class InterviewRepositoryImpl @Inject constructor(
    private val providerManager: ProviderManager
) : InterviewRepository {

    private val messages = MutableStateFlow<List<InterviewMessage>>(emptyList())
    private var systemPrompt = ""

    private fun getProvider(): AIProvider {
        return providerManager.getBestProviderFor(ProviderCapability.AI.Chat) as? AIProvider
            ?: throw Exception("No AI provider available")
    }

    override fun startSession(role: String, difficulty: String): Flow<InterviewMessage> = flow {
        systemPrompt = """
            You are a professional interviewer for the position of $role.
            The interview difficulty level is $difficulty.
            Conduct a realistic interview by asking one question at a time.
            After the user answers, provide brief feedback if necessary and then ask the next question.
            Keep the conversation professional and focused on the job role.
        """.trimIndent()

        messages.value = emptyList()
        
        val initialAiMessage = getAiResponse()
        emit(initialAiMessage)
    }

    override fun sendMessage(text: String): Flow<InterviewMessage> = flow {
        val userMessage = InterviewMessage(role = MessageRole.User, text = text)
        messages.update { it + userMessage }
        
        val aiResponse = getAiResponse()
        emit(aiResponse)
    }

    override fun getFeedback(): Flow<InterviewFeedback> = flow {
        val prompt = """
            Based on the following interview transcript, please provide structured feedback.
            Format the response as JSON with the following keys:
            - summary: A general overview of performance.
            - strengths: A list of key strengths.
            - weaknesses: A list of areas for improvement.
            - tips: Practical tips for future interviews.

            Transcript:
            ${messages.value.joinToString("\n") { "${it.role}: ${it.text}" }}
        """.trimIndent()

        val provider = getProvider()
        val result = provider.generateText(prompt).getOrNull()
        if (result != null) {
            emit(parseFeedback(result))
        }
    }

    private suspend fun getAiResponse(): InterviewMessage {
        val sdkHistory = mutableListOf(SdkAiMessage(CommonMessageRole.SYSTEM, systemPrompt))
        sdkHistory.addAll(messages.value.map { msg ->
            SdkAiMessage(
                role = if (msg.role == MessageRole.User) CommonMessageRole.USER else CommonMessageRole.ASSISTANT,
                content = msg.text
            )
        })

        val provider = getProvider()
        val response = provider.chat(sdkHistory).getOrElse { "I'm sorry, I couldn't process that." }
        val aiMessage = InterviewMessage(role = MessageRole.AI, text = response)
        messages.update { it + aiMessage }
        return aiMessage
    }

    private fun parseFeedback(json: String): InterviewFeedback {
        return InterviewFeedback(
            summary = "Good overall performance.",
            strengths = listOf("Clear communication", "Technical knowledge"),
            weaknesses = listOf("Could be more concise"),
            tips = listOf("Practice more coding problems")
        )
    }
}
