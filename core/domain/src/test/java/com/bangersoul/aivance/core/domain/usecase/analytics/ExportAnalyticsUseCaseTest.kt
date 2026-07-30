package com.bangersoul.aivance.core.domain.usecase.analytics

import com.bangersoul.aivance.core.common.model.AnalyticsEvent
import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExportAnalyticsUseCaseTest {

    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var useCase: ExportAnalyticsUseCase

    @Before
    fun setUp() {
        analyticsRepository = mockk()
        useCase = ExportAnalyticsUseCase(analyticsRepository)
    }

    @Test
    fun `should export analytics with data`() = runTest {
        val events = listOf(
            AnalyticsEvent(id = "1", eventName = "test", category = "GENERAL", timestamp = System.currentTimeMillis())
        )
        every { analyticsRepository.getEvents() } returns flowOf(Result.Success(events))

        val result = useCase()
        assertTrue(result.isSuccess)
        val report = (result as Result.Success).data
        assertTrue(report.contains("test"))
    }

    @Test
    fun `should handle empty analytics`() = runTest {
        every { analyticsRepository.getEvents() } returns flowOf(Result.Success(emptyList()))

        val result = useCase()
        assertTrue(result.isSuccess)
    }
}
