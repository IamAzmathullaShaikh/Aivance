package com.bangersoul.aivance.core.domain.usecase.analytics

import com.bangersoul.aivance.core.common.result.Result
import com.bangersoul.aivance.core.domain.analytics.AnalyticsEngine
import com.bangersoul.aivance.core.domain.analytics.FeatureCategory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TrackEventUseCaseTest {

    private lateinit var analyticsEngine: AnalyticsEngine
    private lateinit var useCase: TrackEventUseCase

    @Before
    fun setUp() {
        analyticsEngine = mockk()
        useCase = TrackEventUseCase(analyticsEngine)
    }

    @Test
    fun `should track event successfully`() = runTest {
        coEvery { analyticsEngine.trackEvent(any(), any(), any(), any()) } returns Result.Success(Unit)

        val result = useCase(TrackEventRequest(eventName = "resume_engine_import"))

        assertTrue(result.isSuccess)
        coVerify {
            analyticsEngine.trackEvent(
                eventName = "resume_engine_import",
                category = FeatureCategory.RESUME,
                properties = emptyMap(),
                durationMs = null
            )
        }
    }

    @Test
    fun `should fail for blank event name`() = runTest {
        val result = useCase(TrackEventRequest(eventName = ""))

        assertTrue(result.isFailure)
    }

    @Test
    fun `should respect explicit category`() = runTest {
        coEvery { analyticsEngine.trackEvent(any(), any(), any(), any()) } returns Result.Success(Unit)

        val result = useCase(
            TrackEventRequest(eventName = "custom_event", category = FeatureCategory.PROFILE)
        )

        assertTrue(result.isSuccess)
        coVerify {
            analyticsEngine.trackEvent(
                eventName = "custom_event",
                category = FeatureCategory.PROFILE,
                properties = emptyMap(),
                durationMs = null
            )
        }
    }

    @Test
    fun `should infer GENERAL for unknown event names`() = runTest {
        coEvery { analyticsEngine.trackEvent(any(), any(), any(), any()) } returns Result.Success(Unit)

        useCase(TrackEventRequest(eventName = "mystery_action"))

        coVerify {
            analyticsEngine.trackEvent(
                eventName = "mystery_action",
                category = FeatureCategory.GENERAL,
                properties = emptyMap(),
                durationMs = null
            )
        }
    }
}
