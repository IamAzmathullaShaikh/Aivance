package com.bangersoul.aivance.core.domain.usecase.settings

import com.bangersoul.aivance.core.common.result.CoreResult
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoadSettingsUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: LoadSettingsUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk()
        useCase = LoadSettingsUseCase(settingsRepository)
    }

    @Test
    fun `should load settings successfully`() = runTest {
        every { settingsRepository.getAiProviderConfigs() } returns flowOf(Result.Success(emptyList()))

        val result = useCase.invoke().first()
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should return default settings on error`() = runTest {
        every { settingsRepository.getAiProviderConfigs() } returns flowOf(
            Result.Failure(com.bangersoul.aivance.core.common.result.DatabaseError("Error"))
        )

        val result = useCase.invoke().first()
        assertTrue(result.isSuccess)
    }
}
