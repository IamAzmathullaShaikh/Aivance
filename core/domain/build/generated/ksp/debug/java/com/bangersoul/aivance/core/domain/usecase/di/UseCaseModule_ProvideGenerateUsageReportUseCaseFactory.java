package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.AnalyticsRepository;
import com.bangersoul.aivance.core.domain.usecase.analytics.GenerateUsageReportUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class UseCaseModule_ProvideGenerateUsageReportUseCaseFactory implements Factory<GenerateUsageReportUseCase> {
  private final Provider<AnalyticsRepository> analyticsRepositoryProvider;

  private UseCaseModule_ProvideGenerateUsageReportUseCaseFactory(
      Provider<AnalyticsRepository> analyticsRepositoryProvider) {
    this.analyticsRepositoryProvider = analyticsRepositoryProvider;
  }

  @Override
  public GenerateUsageReportUseCase get() {
    return provideGenerateUsageReportUseCase(analyticsRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideGenerateUsageReportUseCaseFactory create(
      Provider<AnalyticsRepository> analyticsRepositoryProvider) {
    return new UseCaseModule_ProvideGenerateUsageReportUseCaseFactory(analyticsRepositoryProvider);
  }

  public static GenerateUsageReportUseCase provideGenerateUsageReportUseCase(
      AnalyticsRepository analyticsRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideGenerateUsageReportUseCase(analyticsRepository));
  }
}
