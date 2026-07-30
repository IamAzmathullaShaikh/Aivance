package com.bangersoul.aivance.core.data.analytics;

import com.bangersoul.aivance.core.database.dao.AiAnalyticsDao;
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
public final class AnalyticsEngineImpl_Factory implements Factory<AnalyticsEngineImpl> {
  private final Provider<AiAnalyticsDao> analyticsDaoProvider;

  private final Provider<TelemetryEngine> telemetryEngineProvider;

  private AnalyticsEngineImpl_Factory(Provider<AiAnalyticsDao> analyticsDaoProvider,
      Provider<TelemetryEngine> telemetryEngineProvider) {
    this.analyticsDaoProvider = analyticsDaoProvider;
    this.telemetryEngineProvider = telemetryEngineProvider;
  }

  @Override
  public AnalyticsEngineImpl get() {
    return newInstance(analyticsDaoProvider.get(), telemetryEngineProvider.get());
  }

  public static AnalyticsEngineImpl_Factory create(Provider<AiAnalyticsDao> analyticsDaoProvider,
      Provider<TelemetryEngine> telemetryEngineProvider) {
    return new AnalyticsEngineImpl_Factory(analyticsDaoProvider, telemetryEngineProvider);
  }

  public static AnalyticsEngineImpl newInstance(AiAnalyticsDao analyticsDao,
      TelemetryEngine telemetryEngine) {
    return new AnalyticsEngineImpl(analyticsDao, telemetryEngine);
  }
}
