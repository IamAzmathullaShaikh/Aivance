package com.bangersoul.aivance.core.domain.usecase.analytics;

import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class TrackEventUseCase_Factory implements Factory<TrackEventUseCase> {
  private final Provider<AnalyticsRepository> analyticsRepositoryProvider;

  private TrackEventUseCase_Factory(Provider<AnalyticsRepository> analyticsRepositoryProvider) {
    this.analyticsRepositoryProvider = analyticsRepositoryProvider;
  }

  @Override
  public TrackEventUseCase get() {
    return newInstance(analyticsRepositoryProvider.get());
  }

  public static TrackEventUseCase_Factory create(
      Provider<AnalyticsRepository> analyticsRepositoryProvider) {
    return new TrackEventUseCase_Factory(analyticsRepositoryProvider);
  }

  public static TrackEventUseCase newInstance(AnalyticsRepository analyticsRepository) {
    return new TrackEventUseCase(analyticsRepository);
  }
}
