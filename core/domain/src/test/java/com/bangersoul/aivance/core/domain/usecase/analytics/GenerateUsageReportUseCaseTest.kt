package com.bangersoul.aivance.core.domain.usecase.analytics

import com.bangersoul.aivance.core.common.model.AnalyticsSnapshot
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
    fun `should generate report with snapshots`() = runTest {
        val snapshots = listOf(
            AnalyticsSnapshot(
                id = 1L,
                careerScore = 85,
                kpis = mapOf("applications" to 3.0, "interviews" to 1.0)
            )
        )
        every { analyticsRepository.getSnapshots() } returns flowOf(Result.Success(snapshots))

        val result = useCase(Unit)
        assertTrue(result.isSuccess)
        val report = (result as Result.Success).data
        assertTrue(report.contains("Career Usage Report"))
        assertTrue(report.contains("Career Score: 85"))
    }

    @Test
    fun `should handle empty snapshots`() = runTest {
        every { analyticsRepository.getSnapshots() } returns flowOf(Result.Success(emptyList()))

        val result = useCase(Unit)
        assertTrue(result.isSuccess)
        val report = (result as Result.Success).data
        assertTrue(report.contains("Career Score: 0"))
    }
}
