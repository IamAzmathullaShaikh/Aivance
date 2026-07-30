package com.bangersoul.aivance.core.domain.usecase.provider

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SelectProviderUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: SelectProviderUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk()
        useCase = SelectProviderUseCase(settingsRepository)
    }

    @Test
    fun `should select Gemini provider`() = runTest {
        coEvery { settingsRepository.updateAiProviderConfig(any()) } returns Result.Success(Unit)

        val result = useCase(SelectProviderRequest(providerId = "GEMINI", apiKey = "test-key"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should select Ollama provider without API key`() = runTest {
        coEvery { settingsRepository.updateAiProviderConfig(any()) } returns Result.Success(Unit)

        val result = useCase(SelectProviderRequest(providerId = "OLLAMA"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for unknown provider`() = runTest {
        val result = useCase(SelectProviderRequest(providerId = "UNKNOWN"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `should fail for blank provider ID`() = runTest {
        val result = useCase(SelectProviderRequest(providerId = ""))
        assertTrue(result.isFailure)
    }
}
