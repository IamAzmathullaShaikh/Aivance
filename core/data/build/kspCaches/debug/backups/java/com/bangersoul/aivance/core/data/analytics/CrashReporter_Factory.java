package com.bangersoul.aivance.core.data.analytics;

import com.bangersoul.aivance.core.domain.analytics.AnalyticsEngine;
import com.bangersoul.aivance.core.domain.telemetry.TelemetryEngine;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class CrashReporter_Factory implements Factory<CrashReporter> {
  private final Provider<AnalyticsEngine> analyticsEngineProvider;

  private final Provider<TelemetryEngine> telemetryEngineProvider;

  private CrashReporter_Factory(Provider<AnalyticsEngine> analyticsEngineProvider,
      Provider<TelemetryEngine> telemetryEngineProvider) {
    this.analyticsEngineProvider = analyticsEngineProvider;
    this.telemetryEngineProvider = telemetryEngineProvider;
  }

  @Override
  public CrashReporter get() {
    return newInstance(analyticsEngineProvider.get(), telemetryEngineProvider.get());
  }

  public static CrashReporter_Factory create(Provider<AnalyticsEngine> analyticsEngineProvider,
      Provider<TelemetryEngine> telemetryEngineProvider) {
    return new CrashReporter_Factory(analyticsEngineProvider, telemetryEngineProvider);
  }

  public static CrashReporter newInstance(AnalyticsEngine analyticsEngine,
      TelemetryEngine telemetryEngine) {
    return new CrashReporter(analyticsEngine, telemetryEngine);
  }
}
