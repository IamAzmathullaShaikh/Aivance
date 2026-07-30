package com.bangersoul.aivance.core.domain.usecase.provider

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAvailableModelsUseCaseTest {

    private lateinit var providerManager: ProviderManager
    private lateinit var useCase: GetAvailableModelsUseCase

    @Before
    fun setUp() {
        providerManager = mockk(relaxed = true)
        useCase = GetAvailableModelsUseCase(providerManager)
    }

    @Test
    fun `should return models for Gemini`() = runTest {
        val result = useCase("GEMINI")
        assertTrue(result.isSuccess)
        assertTrue((result as Result.Success).data.contains("gemini-1.5-flash"))
    }

    @Test
    fun `should return models for OpenAI`() = runTest {
        val result = useCase("OPENAI")
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank provider ID`() = runTest {
        val result = useCase("")
        assertTrue(result.isFailure)
    }
}
