package com.bangersoul.aivance.core.domain.usecase.career

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AiRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GenerateCareerRoadmapUseCaseTest {

    private lateinit var aiRepository: AiRepository
    private lateinit var useCase: GenerateCareerRoadmapUseCase

    @Before
    fun setUp() {
        aiRepository = mockk()
        useCase = GenerateCareerRoadmapUseCase(aiRepository)
    }

    @Test
    fun `should generate roadmap successfully`() = runTest {
        coEvery { aiRepository.analyzeText(any(), any()) } returns Result.Success("1. Learn Kotlin\n2. Build Android apps\n3. Lead a team")

        val result = useCase(GenerateCareerRoadmapRequest(targetRole = "Senior Android Developer"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank target role`() = runTest {
        val result = useCase(GenerateCareerRoadmapRequest(targetRole = ""))
        assertTrue(result.isFailure)
    }
}
