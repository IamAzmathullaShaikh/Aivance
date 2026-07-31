package com.bangersoul.aivance.core.database.converter

import com.bangersoul.aivance.core.common.enums.AIModel
import com.bangersoul.aivance.core.common.enums.MessageRole
import com.bangersoul.aivance.core.common.enums.MessageSender
import com.bangersoul.aivance.core.common.model.AIMessage
import com.bangersoul.aivance.core.common.model.InterviewFeedback
import com.bangersoul.aivance.core.common.model.InterviewMessage
import com.bangersoul.aivance.core.common.model.ProviderCapability
import com.bangersoul.aivance.core.common.model.ResumeSection
import com.bangersoul.aivance.core.common.model.RoadmapStep
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

class AivanceConvertersTest {

    private lateinit var converters: AivanceConverters

    @Before
    fun setup() {
        converters = AivanceConverters()
    }

    @Test
    fun uuidConversion() {
        val uuid = UUID.randomUUID()
        val string = converters.fromUUID(uuid)
        val convertedBack = converters.toUUID(string)
        assertThat(convertedBack).isEqualTo(uuid)
    }

    @Test
    fun instantConversion() {
        val instant = Instant.ofEpochMilli(System.currentTimeMillis())
        val millis = converters.fromInstant(instant)
        val convertedBack = converters.toInstant(millis)
        assertThat(convertedBack).isEqualTo(instant)
    }

    @Test
    fun localDateTimeConversion() {
        val dateTime = LocalDateTime.now().withNano(0) // Precision can be lost in some formats, so avoid nanos for simple check
        val string = converters.fromLocalDateTime(dateTime)
        val convertedBack = converters.toLocalDateTime(string)
        assertThat(convertedBack).isEqualTo(dateTime)
    }

    @Test
    fun stringListConversion() {
        val list = listOf("one", "two", "three")
        val string = converters.fromStringList(list)
        val convertedBack = converters.toStringList(string)
        assertThat(convertedBack).isEqualTo(list)
    }

    @Test
    fun resumeSectionListConversion() {
        val sections = listOf(
            ResumeSection(sectionType = "EXPERIENCE", title = "Exp", content = "Content 1"),
            ResumeSection(sectionType = "EDUCATION", title = "Edu", content = "Content 2")
        )
        val string = converters.fromResumeSectionList(sections)
        val convertedBack = converters.toResumeSectionList(string)
        assertThat(convertedBack).isEqualTo(sections)
    }

    @Test
    fun interviewMessageListConversion() {
        val messages = listOf(
            InterviewMessage(id = "1", sessionId = "session-1", sender = MessageSender.AI_INTERVIEWER, text = "Hello"),
            InterviewMessage(id = "2", sessionId = "session-1", sender = MessageSender.USER, text = "Hi")
        )
        val string = converters.fromInterviewMessageList(messages)
        val convertedBack = converters.toInterviewMessageList(string)
        assertThat(convertedBack).isEqualTo(messages)
    }

    @Test
    fun interviewFeedbackConversion() {
        val feedback = InterviewFeedback(
            overallScore = 85,
            strengths = listOf("Communication"),
            improvements = listOf("Tech depth"),
            detailedSummary = "Good job"
        )
        val string = converters.fromInterviewFeedback(feedback)
        val convertedBack = converters.toInterviewFeedback(string)
        assertThat(convertedBack).isEqualTo(feedback)
    }

    @Test
    fun roadmapStepListConversion() {
        val steps = listOf(
            RoadmapStep(id = 1, title = "Step 1", isCompleted = true),
            RoadmapStep(id = 2, title = "Step 2", isCompleted = false)
        )
        val string = converters.fromRoadmapStepList(steps)
        val convertedBack = converters.toRoadmapStepList(string)
        assertThat(convertedBack).isEqualTo(steps)
    }

    @Test
    fun aiMessageListConversion() {
        val messages = listOf(
            AIMessage("1", "conv1", MessageRole.SYSTEM, "Prompt"),
            AIMessage("2", "conv1", MessageRole.USER, "Question")
        )
        val string = converters.fromAIMessageList(messages)
        val convertedBack = converters.toAIMessageList(string)
        assertThat(convertedBack).isEqualTo(messages)
    }

    @Test
    fun aiModelListConversion() {
        val models = listOf(AIModel.GEMINI_1_5_FLASH, AIModel.GPT_4O)
        val string = converters.fromAIModelList(models)
        val convertedBack = converters.toAIModelList(string)
        assertThat(convertedBack).isEqualTo(models)
    }

    @Test
    fun providerCapabilityListConversion() {
        val capabilities = listOf(
            ProviderCapability("Streaming", true),
            ProviderCapability("Vision", false)
        )
        val string = converters.fromProviderCapabilityList(capabilities)
        val convertedBack = converters.toProviderCapabilityList(string)
        assertThat(convertedBack).isEqualTo(capabilities)
    }

    @Test
    fun stringMapConversion() {
        val map = mapOf("key1" to "value1", "key2" to "value2")
        val string = converters.fromStringMap(map)
        val convertedBack = converters.toStringMap(string)
        assertThat(convertedBack).isEqualTo(map)
    }
}
