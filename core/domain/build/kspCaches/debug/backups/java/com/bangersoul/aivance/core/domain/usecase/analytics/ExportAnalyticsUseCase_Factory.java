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
public final class ExportAnalyticsUseCase_Factory implements Factory<ExportAnalyticsUseCase> {
  private final Provider<AnalyticsRepository> analyticsRepositoryProvider;

  private ExportAnalyticsUseCase_Factory(
      Provider<AnalyticsRepository> analyticsRepositoryProvider) {
    this.analyticsRepositoryProvider = analyticsRepositoryProvider;
  }

  @Override
  public ExportAnalyticsUseCase get() {
    return newInstance(analyticsRepositoryProvider.get());
  }

  public static ExportAnalyticsUseCase_Factory create(
      Provider<AnalyticsRepository> analyticsRepositoryProvider) {
    return new ExportAnalyticsUseCase_Factory(analyticsRepositoryProvider);
  }

  public static ExportAnalyticsUseCase newInstance(AnalyticsRepository analyticsRepository) {
    return new ExportAnalyticsUseCase(analyticsRepository);
  }
}
