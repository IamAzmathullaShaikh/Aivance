package com.bangersoul.aivance.core.data.analytics;

import android.content.Context;
import com.bangersoul.aivance.core.domain.telemetry.TelemetryEngine;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class PerformanceCollector_Factory implements Factory<PerformanceCollector> {
  private final Provider<Context> appContextProvider;

  private final Provider<TelemetryEngine> telemetryEngineProvider;

  private PerformanceCollector_Factory(Provider<Context> appContextProvider,
      Provider<TelemetryEngine> telemetryEngineProvider) {
    this.appContextProvider = appContextProvider;
    this.telemetryEngineProvider = telemetryEngineProvider;
  }

  @Override
  public PerformanceCollector get() {
    return newInstance(appContextProvider.get(), telemetryEngineProvider.get());
  }

  public static PerformanceCollector_Factory create(Provider<Context> appContextProvider,
      Provider<TelemetryEngine> telemetryEngineProvider) {
    return new PerformanceCollector_Factory(appContextProvider, telemetryEngineProvider);
  }

  public static PerformanceCollector newInstance(Context appContext,
      TelemetryEngine telemetryEngine) {
    return new PerformanceCollector(appContext, telemetryEngine);
  }
}
