package com.bangersoul.aivance.core.data.telemetry;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class TelemetryEngineImpl_Factory implements Factory<TelemetryEngineImpl> {
  @Override
  public TelemetryEngineImpl get() {
    return newInstance();
  }

  public static TelemetryEngineImpl_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TelemetryEngineImpl newInstance() {
    return new TelemetryEngineImpl();
  }

  private static final class InstanceHolder {
    static final TelemetryEngineImpl_Factory INSTANCE = new TelemetryEngineImpl_Factory();
  }
}
