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
        val snapshots = listOf(
            AnalyticsSnapshot(id = 1L, careerScore = 85, kpis = mapOf("applications" to 3.0))
        )
        every { analyticsRepository.getSnapshots() } returns flowOf(Result.Success(snapshots))

        val result = useCase(Unit)
        assertTrue(result.isSuccess)
        val report = (result as Result.Success).data
        assertTrue(report.isNotBlank())
        assertTrue(report.contains("careerScore"))
    }

    @Test
    fun `should handle empty analytics`() = runTest {
        every { analyticsRepository.getSnapshots() } returns flowOf(Result.Success(emptyList()))

        val result = useCase(Unit)
        assertTrue(result.isSuccess)
    }
}
