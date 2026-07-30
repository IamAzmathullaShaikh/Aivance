package com.bangersoul.aivance.core.data.repository

import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.AtsResult
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeSection
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.data.source.ResumeLocalDataSource
import com.bangersoul.aivance.sdk.api.AIProvider
import com.bangersoul.aivance.sdk.core.ProviderCapability
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ResumeRepositoryImplTest {

    private lateinit var repository: ResumeRepositoryImpl
    private val localDataSource: ResumeLocalDataSource = mockk()
    private val providerManager: ProviderManager = mockk()
    private val mockAIProvider: AIProvider = mockk()

    @Before
    fun setUp() {
        every { providerManager.getBestProviderFor(ProviderCapability.AI.Chat) } returns mockAIProvider
        repository = ResumeRepositoryImpl(localDataSource, providerManager)
    }

    @Test
    fun `getResumes returns success with list of resumes`() = runTest {
        val resumes = listOf(Resume(id = 1, fileName = "resume.pdf", fileUri = "", rawText = "text"))
        every { localDataSource.getResumes() } returns flowOf(resumes)

        repository.getResumes().test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(resumes, (result as Result.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun `getResumeById returns success when resume exists`() = runTest {
        val resumeId = 1L
        val resumes = listOf(Resume(id = resumeId, fileName = "resume.pdf", fileUri = "", rawText = "text"))
        every { localDataSource.getResumes() } returns flowOf(resumes)

        repository.getResumeById(resumeId).test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(resumes.first(), (result as Result.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun `getResumeById returns failure when resume does not exist`() = runTest {
        val resumeId = 1L
        every { localDataSource.getResumes() } returns flowOf(emptyList())

        repository.getResumeById(resumeId).test {
            val result = awaitItem()
            assertTrue(result is Result.Failure)
            assertEquals("Resume not found", (result as Result.Failure).error.message)
            awaitComplete()
        }
    }

    @Test
    fun `insertResume calls localDataSource and returns id`() = runTest {
        val resume = Resume(id = 1, fileName = "new.pdf", fileUri = "", rawText = "text")
        coEvery { localDataSource.saveResume(any()) } returns Unit

        val result = repository.insertResume(resume)

        assertTrue(result.isSuccess)
        coVerify { localDataSource.saveResume(resume) }
    }

    @Test
    fun `updateResume calls localDataSource`() = runTest {
        val resume = Resume(id = 1, fileName = "updated.pdf", fileUri = "", rawText = "text")
        coEvery { localDataSource.saveResume(any()) } returns Unit

        val result = repository.updateResume(resume)

        assertTrue(result.isSuccess)
        coVerify { localDataSource.saveResume(resume) }
    }

    @Test
    fun `deleteResume calls localDataSource when resume exists`() = runTest {
        val resumeId = 1L
        val resume = Resume(id = resumeId, fileName = "delete.pdf", fileUri = "", rawText = "text")
        coEvery { localDataSource.getResumeById(resumeId) } returns resume
        coEvery { localDataSource.deleteResume(resume) } returns Unit

        val result = repository.deleteResume(resumeId)

        assertTrue(result.isSuccess)
        coVerify { localDataSource.deleteResume(resume) }
    }

    @Test
    fun `updateSections updates resume with new sections`() = runTest {
        val resumeId = 1L
        val resume = Resume(id = resumeId, fileName = "resume.pdf", fileUri = "", rawText = "text")
        val sections = listOf(ResumeSection("WORK", "Title", "Content"))
        coEvery { localDataSource.getResumeById(resumeId) } returns resume
        coEvery { localDataSource.saveResume(any()) } returns Unit

        val result = repository.updateSections(resumeId, sections)

        assertTrue(result.isSuccess)
        coVerify { localDataSource.saveResume(resume.copy(sections = sections)) }
    }

    @Test
    fun `analyzeResume returns analysis from AI provider`() = runTest {
        val resumeId = 1L
        val resume = Resume(id = resumeId, fileName = "resume.pdf", fileUri = "", rawText = "resume text")
        val jobDescription = "job description"
        coEvery { localDataSource.getResumeById(resumeId) } returns resume
        coEvery { mockAIProvider.generateText(any()) } returns Result.Success("AI feedback")

        val result = repository.analyzeResume(resumeId, jobDescription)

        assertTrue(result.isSuccess)
        assertEquals(80, result.getOrNull()?.overallScore)
        assertEquals("AI feedback", result.getOrNull()?.matchSummary)
    }

    @Test
    fun `getAtsResults returns results from localDataSource`() = runTest {
        val atsResults = listOf(AtsResult(score = 90, resumeName = "resume.pdf", feedback = "Good"))
        every { localDataSource.getAtsResults() } returns flowOf(atsResults)

        repository.getAtsResults(1L).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(atsResults, result.getOrNull())
            awaitComplete()
        }
    }
}
