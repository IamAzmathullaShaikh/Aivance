package com.bangersoul.aivance.feature.resume.data.repository

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.result.DomainError
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.database.dao.AtsDao
import com.bangersoul.aivance.core.domain.service.TextGenerationService
import com.bangersoul.aivance.feature.resume.domain.model.KeywordInfo
import com.bangersoul.aivance.feature.resume.domain.model.OptimizationTip
import com.bangersoul.aivance.feature.resume.domain.model.ResumeAnalysis
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ResumeRepositoryTest {

    private lateinit var textGenerationService: TextGenerationService
    private lateinit var atsDao: AtsDao
    private lateinit var repository: ResumeRepositoryImpl

    @Before
    fun setup() {
        textGenerationService = mockk()
        atsDao = mockk()
        repository = ResumeRepositoryImpl(textGenerationService, atsDao)
    }

    @Test
    fun `analyzeResume emits analysis when AI service returns valid JSON`() = runTest {
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

        coEvery { textGenerationService.generateText(any()) } returns Result.Success(mockResponse)

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
        coEvery { textGenerationService.generateText(any()) } returns Result.Failure(
            DomainError("Network error")
        )

        repository.analyzeResume("Resume", "Job").test {
            val error = awaitError()
            assertThat(error.message).isEqualTo("Network error")
        }
    }

    @Test
    fun `analyzeResume throws exception when AI response is invalid JSON`() = runTest {
        coEvery { textGenerationService.generateText(any()) } returns Result.Success("Invalid JSON")

        repository.analyzeResume("Resume", "Job").test {
            val error = awaitError()
            assertThat(error.message).contains("Failed to parse AI response")
        }
    }

    @Test
    fun `analyzeResume strips markdown fences around AI response`() = runTest {
        val wrappedResponse = """
            ```json
            {
              "matchScore": 70,
              "keywords": [],
              "tips": []
            }
            ```
        """.trimIndent()

        coEvery { textGenerationService.generateText(any()) } returns Result.Success(wrappedResponse)

        repository.analyzeResume("Resume", "Job").test {
            val result = awaitItem()
            assertThat(result.matchScore).isEqualTo(70)
            awaitComplete()
        }
    }

    @Test
    fun `saveAnalysis inserts entity into ATS dao`() = runTest {
        val analysis = ResumeAnalysis(
            matchScore = 80,
            keywords = listOf(KeywordInfo("Kotlin", true)),
            tips = listOf(OptimizationTip("Skills", "Add more examples"))
        )
        coEvery { atsDao.insertAtsResult(any()) } returns 1L

        repository.saveAnalysis(analysis, resumeId = 1L, jobDescription = "Job")

        coVerify { atsDao.insertAtsResult(any()) }
    }
}
