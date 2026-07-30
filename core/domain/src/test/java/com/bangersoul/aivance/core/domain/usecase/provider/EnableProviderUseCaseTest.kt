package com.bangersoul.aivance.core.domain.usecase.provider

import com.bangersoul.aivance.core.common.model.AiProviderConfig
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EnableProviderUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: EnableProviderUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk()
        useCase = EnableProviderUseCase(settingsRepository)
    }

    @Test
    fun `should enable provider successfully`() = runTest {
        val config = AiProviderConfig(providerId = "GEMINI", apiKey = "key", selectedModel = "gemini-1.5-flash")
        coEvery { settingsRepository.updateAiProviderConfig(any()) } returns Result.Success(Unit)

        val result = useCase(config)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank provider ID`() = runTest {
        val config = AiProviderConfig(providerId = "", apiKey = "key", selectedModel = "model")
        val result = useCase(config)
        assertTrue(result.isFailure)
    }
}
