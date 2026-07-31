package com.bangersoul.aivance.core.data.resume

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ResumeParserTest {

    private val providerManager = mockk<ProviderManager>()
    private val aiProvider = mockk<AIProvider>()
    private lateinit var resumeParser: ResumeParser

    @Before
    fun setup() {
        resumeParser = ResumeParser(providerManager)
        coEvery { providerManager.getBestProviderFor(ProviderCapability.AI.Chat) } returns aiProvider
    }

    @Test
    fun `parseRawText should return structured sections when AI returns valid JSON`() = runTest {
        val rawText = "John Doe\nExperience: Software Engineer at X"
        val aiResponse = """
            [
                {"sectionType": "summary", "title": "Summary", "content": "Experienced engineer"},
                {"sectionType": "experience", "title": "Experience", "content": "Software Engineer at X"}
            ]
        """.trimIndent()

        coEvery { aiProvider.generateText(any()) } returns Result.Success(aiResponse)

        val sections = resumeParser.parseRawText(rawText)

        assertEquals(2, sections.size)
        assertEquals("summary", sections[0].sectionType)
        assertEquals("Summary", sections[0].title)
        assertEquals("Experienced engineer", sections[0].content)
    }

    @Test
    fun `parseRawText should handle markdown JSON blocks`() = runTest {
        val aiResponse = "Here is the JSON: ```json [{\"sectionType\": \"skills\", \"title\": \"Skills\", \"content\": \"Kotlin, Swift\"}] ```"

        coEvery { aiProvider.generateText(any()) } returns Result.Success(aiResponse)

        val sections = resumeParser.parseRawText("some text")

        assertEquals(1, sections.size)
        assertEquals("skills", sections[0].sectionType)
        assertEquals("Kotlin, Swift", sections[0].content)
    }
}
