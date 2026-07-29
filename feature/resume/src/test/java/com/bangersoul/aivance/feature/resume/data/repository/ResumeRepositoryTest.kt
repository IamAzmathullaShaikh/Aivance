package com.bangersoul.aivance.feature.resume.data.repository

import app.cash.turbine.test
import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.network.AiService
import com.bangersoul.aivance.feature.resume.domain.repository.ResumeRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ResumeRepositoryTest {

    private lateinit var aiService: AiService
    private lateinit var atsDao: AtsDao
    private lateinit var repository: ResumeRepository

    @Before
    fun setup() {
        aiService = mockk()
        atsDao = mockk()
        repository = ResumeRepositoryImpl(aiService, atsDao)
    }

    @Test
    fun `analyzeResume returns success when AI service returns valid JSON`() = runTest {
        val resumeText = "Experienced developer"
        val jobDescription = "Looking for a developer"
        val mockResponse = """
            {
              "matchScore": 85,
              "keywords": [
                {"text": "Kotlin", "isMatched": true}
              ],
              "tips": [
                {"category": "Skills", "description": "Good job"}
              ]
            }
        """.trimIndent()

        coEvery { aiService.analyzeText(any()) } returns Result.success(mockResponse)

        repository.analyzeResume(resumeText, jobDescription).test {
            val result = awaitItem()
            assertThat(result.matchScore).isEqualTo(85)
            assertThat(result.keywords).hasSize(1)
            assertThat(result.keywords[0].text).isEqualTo("Kotlin")
            assertThat(result.keywords[0].isMatched).isTrue()
            awaitComplete()
        }
    }

    @Test
    fun `analyzeResume throws exception when AI service fails`() = runTest {
        val resumeText = "Experienced developer"
        val jobDescription = "Looking for a developer"
        val exception = Exception("Network error")

        coEvery { aiService.analyzeText(any()) } returns Result.failure(exception)

        repository.analyzeResume(resumeText, jobDescription).test {
            val error = awaitError()
            assertThat(error.message).isEqualTo("Network error")
        }
    }

    @Test
    fun `analyzeResume throws exception when AI response is invalid JSON`() = runTest {
        val resumeText = "Experienced developer"
        val jobDescription = "Looking for a developer"
        val mockResponse = "Invalid JSON"

        coEvery { aiService.analyzeText(any()) } returns Result.success(mockResponse)

        repository.analyzeResume(resumeText, jobDescription).test {
            val error = awaitError()
            assertThat(error.message).contains("Failed to parse AI response")
        }
    }
}
