package com.bangersoul.aivance.feature.resume

import app.cash.turbine.test
import com.bangersoul.aivance.feature.resume.domain.model.ResumeAnalysis
import com.bangersoul.aivance.feature.resume.domain.repository.ResumeRepository
import com.bangersoul.aivance.feature.tracker.domain.JobTrackerRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResumeViewModelTest {

    private lateinit var repository: ResumeRepository
    private lateinit var trackerRepository: JobTrackerRepository
    private lateinit var viewModel: ResumeViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        trackerRepository = mockk()
        viewModel = ResumeViewModel(repository, trackerRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `analyzeResume triggers state transitions correctly`() = runTest {
        val resumeText = "Resume"
        val jobDescription = "Job"
        val mockAnalysis = ResumeAnalysis(85, emptyList(), emptyList())

        every { repository.analyzeResume(resumeText, jobDescription) } returns flowOf(mockAnalysis)

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(ResumeUiState.Idle)
            
            viewModel.analyzeResume(resumeText, jobDescription)
            
            // Run until the coroutine starts and finishes collecting
            advanceUntilIdle()
            
            assertThat(awaitItem()).isEqualTo(ResumeUiState.Analyzing)
            assertThat(awaitItem()).isEqualTo(ResumeUiState.Success(mockAnalysis))
        }
    }

    @Test
    fun `analyzeResume sets error state when repository fails`() = runTest {
        val resumeText = "Resume"
        val jobDescription = "Job"
        val errorMessage = "Repository error"

        every { repository.analyzeResume(resumeText, jobDescription) } returns flow {
            throw Exception(errorMessage)
        }

        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(ResumeUiState.Idle)
            
            viewModel.analyzeResume(resumeText, jobDescription)
            
            advanceUntilIdle()
            
            assertThat(awaitItem()).isEqualTo(ResumeUiState.Analyzing)
            val errorState = awaitItem() as ResumeUiState.Error
            assertThat(errorState.message).isEqualTo(errorMessage)
        }
    }

    @Test
    fun `analyzeResume sets error state when inputs are blank`() = runTest {
        viewModel.uiState.test {
            assertThat(awaitItem()).isEqualTo(ResumeUiState.Idle)
            
            viewModel.analyzeResume("", "")
            
            runCurrent()
            
            val errorState = awaitItem() as ResumeUiState.Error
            assertThat(errorState.message).isEqualTo("Resume and Job Description cannot be empty")
        }
    }
}
