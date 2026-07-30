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

class GenerateUsageReportUseCaseTest {

    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var useCase: GenerateUsageReportUseCase

    @Before
    fun setUp() {
        analyticsRepository = mockk()
        useCase = GenerateUsageReportUseCase(analyticsRepository)
    }

    @Test
    fun `should generate report with events`() = runTest {
        val events = listOf(
            AnalyticsEvent(id = "1", eventName = "login", category = "AUTH"),
            AnalyticsEvent(id = "2", eventName = "search", category = "JOBS"),
            AnalyticsEvent(id = "3", eventName = "apply", category = "JOBS")
        )
        every { analyticsRepository.getEvents() } returns flowOf(Result.Success(events))

        val result = useCase()
        assertTrue(result.isSuccess)
        val report = (result as Result.Success).data
        assertTrue(report.totalEvents == 3)
    }

    @Test
    fun `should handle empty events`() = runTest {
        every { analyticsRepository.getEvents() } returns flowOf(Result.Success(emptyList()))

        val result = useCase()
        assertTrue(result.isSuccess)
    }
}
