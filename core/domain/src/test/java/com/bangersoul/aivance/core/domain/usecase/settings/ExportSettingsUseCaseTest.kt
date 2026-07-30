package com.bangersoul.aivance.core.domain.usecase.settings

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportSettingsUseCaseTest {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var useCase: ExportSettingsUseCase

    @Before
    fun setUp() {
        settingsRepository = mockk()
        useCase = ExportSettingsUseCase(settingsRepository)
    }

    @Test
    fun `should export settings as JSON`() = runTest {
        every { settingsRepository.getAiProviderConfigs() } returns flowOf(Result.Success(emptyList()))

        val result = useCase()
        assertTrue(result.isSuccess)
        val json = (result as Result.Success).data
        assertTrue(json.contains("exportVersion"))
    }
}
