package com.bangersoul.aivance.core.domain.usecase.interview

import com.bangersoul.aivance.core.common.enums.InterviewDifficulty
import com.bangersoul.aivance.core.common.model.InterviewSession
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.InterviewRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class StartInterviewSessionUseCaseTest {

    private lateinit var interviewRepository: InterviewRepository
    private lateinit var useCase: StartInterviewSessionUseCase

    @Before
    fun setUp() {
        interviewRepository = mockk()
        useCase = StartInterviewSessionUseCase(interviewRepository)
    }

    @Test
    fun `should start session successfully`() = runTest {
        val session = InterviewSession(id = "1", targetRole = "Android Developer")
        coEvery { interviewRepository.startSession(any(), any(), any(), any(), any(), any()) } returns Result.Success(session)

        val result = useCase(StartInterviewSessionRequest(targetRole = "Android Developer"))

        assertTrue(result.isSuccess)
        assertEquals("1", (result as Result.Success).data.id)
        coVerify {
            interviewRepository.startSession(
                role = "Android Developer",
                company = "",
                difficulty = InterviewDifficulty.MEDIUM,
                jobId = null,
                resumeVersionId = null,
                type = "BEHAVIORAL"
            )
        }
    }

    @Test
    fun `should fail for blank role`() = runTest {
        val result = useCase(StartInterviewSessionRequest(targetRole = ""))

        assertTrue(result.isFailure)
    }
}
