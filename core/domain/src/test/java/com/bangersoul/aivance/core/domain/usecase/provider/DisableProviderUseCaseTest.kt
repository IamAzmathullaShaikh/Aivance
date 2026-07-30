package com.bangersoul.aivance.core.domain.usecase.provider

import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DisableProviderUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: DisableProviderUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk()
        useCase = DisableProviderUseCase(settingsRepository)
    }

    @Test
    fun `should disable provider successfully`() = runTest {
        val config = AiProviderConfig(providerId = "GEMINI", apiKey = "key", selectedModel = "model", isEnabled = true)

        every { settingsRepository.getAiProviderConfigs() } returns flowOf(Result.Success(listOf(config)))
        coEvery { settingsRepository.updateAiProviderConfig(any()) } returns Result.Success(Unit)

        val result = useCase("GEMINI")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank provider ID`() = runTest {
        val result = useCase("")
        assertTrue(result.isFailure)
    }
}
