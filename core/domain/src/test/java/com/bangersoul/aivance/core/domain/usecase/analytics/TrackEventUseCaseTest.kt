package com.bangersoul.aivance.core.domain.usecase.analytics

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TrackEventUseCaseTest {

    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var useCase: TrackEventUseCase

    @Before
    fun setUp() {
        analyticsRepository = mockk()
        useCase = TrackEventUseCase(analyticsRepository)
    }

    @Test
    fun `should track event successfully`() = runTest {
        val result = useCase(TrackEventRequest(eventName = "test_event"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun `should fail for blank event name`() = runTest {
        val result = useCase(TrackEventRequest(eventName = ""))
        assertTrue(result.isFailure)
    }
}
