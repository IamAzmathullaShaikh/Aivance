package com.bangersoul.aivance.core.data.repository

import android.content.Context
import app.cash.turbine.test
import com.bangersoul.aivance.core.common.model.AtsResult
import com.bangersoul.aivance.core.common.model.Resume
import com.bangersoul.aivance.core.common.model.ResumeVersion
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.common.result.getOrNull
import com.bangersoul.aivance.core.data.resume.ResumeParser
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
    private val context: Context = mockk()
    private val localDataSource: ResumeLocalDataSource = mockk()
    private val providerManager: ProviderManager = mockk()
    private val resumeParser: ResumeParser = mockk()
    private val mockAIProvider: AIProvider = mockk()

    @Before
    fun setUp() {
        repository = ResumeRepositoryImpl(
            context = context,
            localDataSource = localDataSource,
            providerManager = providerManager,
            resumeParser = resumeParser
        )
    }

    private fun sampleResume(id: Long = 1L, fileName: String = "resume.pdf") = Resume(
        id = id,
        name = fileName.substringBeforeLast("."),
        fileName = fileName,
        fileUri = "",
        rawText = "text"
    )

    @Test
    fun `getResumes returns success with list of resumes`() = runTest {
        val resumes = listOf(sampleResume())
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
        val resumes = listOf(sampleResume(resumeId))
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

        // runCatchingCore wraps every Throwable into Result.Failure.
        repository.getResumeById(resumeId).test {
            val result = awaitItem()
            assertTrue(result.isFailure)
            assertEquals("Resume not found", (result as Result.Failure).error.message)
            awaitComplete()
        }
    }

    @Test
    fun `saveResume calls localDataSource and returns id`() = runTest {
        val resume = sampleResume()
        coEvery { localDataSource.saveResume(resume) } returns 1L

        val result = repository.saveResume(resume)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify { localDataSource.saveResume(resume) }
    }

    @Test
    fun `deleteResume calls localDataSource when resume exists`() = runTest {
        val resumeId = 1L
        val resume = sampleResume(resumeId)
        coEvery { localDataSource.getResumeById(resumeId) } returns resume
        coEvery { localDataSource.deleteResume(resume) } returns Unit

        val result = repository.deleteResume(resumeId)

        assertTrue(result.isSuccess)
        coVerify { localDataSource.deleteResume(resume) }
    }

    @Test
    fun `getVersions returns versions from localDataSource`() = runTest {
        val versions = listOf(ResumeVersion(resumeId = 1L, versionName = "Original Import"))
        every { localDataSource.getVersionsForResume(1L) } returns flowOf(versions)

        repository.getVersions(1L).test {
            val result = awaitItem()
            assertTrue(result is Result.Success)
            assertEquals(versions, (result as Result.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun `analyzeResume returns analysis from AI provider`() = runTest {
        val resumeId = 1L
        val versionId = 1L
        val version = ResumeVersion(id = versionId, resumeId = resumeId, versionName = "Original Import")
        coEvery { localDataSource.getVersionsForResume(resumeId) } returns flowOf(listOf(version))
        every { providerManager.getBestProviderFor(ProviderCapability.AI.Chat) } returns mockAIProvider
        coEvery { mockAIProvider.generateText(any()) } returns Result.Success("AI feedback")

        val result = repository.analyzeResume(resumeId, versionId, "job description")

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
