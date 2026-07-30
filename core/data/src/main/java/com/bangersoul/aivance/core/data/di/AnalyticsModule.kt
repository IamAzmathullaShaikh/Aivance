package com.bangersoul.aivance.core.data.di

import com.bangersoul.aivance.core.data.analytics.AnalyticsEngineImpl
import com.bangersoul.aivance.core.data.analytics.CrashReporter
import com.bangersoul.aivance.core.data.analytics.PerformanceCollector
import com.bangersoul.aivance.core.data.telemetry.TelemetryEngineImpl
import com.bangersoul.aivance.core.domain.analytics.AnalyticsEngine
import com.bangersoul.aivance.core.domain.telemetry.TelemetryEngine
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds analytics and telemetry interfaces to their implementations.
 *
 * All analytics/telemetry components are @Singleton scoped so that metrics
 * accumulate correctly across the app's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsEngine(
        impl: AnalyticsEngineImpl
    ): AnalyticsEngine

    @Binds
    @Singleton
    abstract fun bindTelemetryEngine(
        impl: TelemetryEngineImpl
    ): TelemetryEngine
}
