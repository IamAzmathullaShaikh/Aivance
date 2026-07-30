package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.JobRepository;
import com.bangersoul.aivance.core.domain.usecase.job.SearchJobsUseCase;
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
public final class UseCaseModule_ProvideSearchJobsUseCaseFactory implements Factory<SearchJobsUseCase> {
  private final Provider<JobRepository> jobRepositoryProvider;

  private UseCaseModule_ProvideSearchJobsUseCaseFactory(
      Provider<JobRepository> jobRepositoryProvider) {
    this.jobRepositoryProvider = jobRepositoryProvider;
  }

  @Override
  public SearchJobsUseCase get() {
    return provideSearchJobsUseCase(jobRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideSearchJobsUseCaseFactory create(
      Provider<JobRepository> jobRepositoryProvider) {
    return new UseCaseModule_ProvideSearchJobsUseCaseFactory(jobRepositoryProvider);
  }

  public static SearchJobsUseCase provideSearchJobsUseCase(JobRepository jobRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideSearchJobsUseCase(jobRepository));
  }
}
