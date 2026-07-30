package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository;
import com.bangersoul.aivance.core.domain.usecase.job.SearchSavedJobsUseCase;
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
public final class UseCaseModule_ProvideSearchSavedJobsUseCaseFactory implements Factory<SearchSavedJobsUseCase> {
  private final Provider<JobTrackerRepository> jobTrackerRepositoryProvider;

  private UseCaseModule_ProvideSearchSavedJobsUseCaseFactory(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    this.jobTrackerRepositoryProvider = jobTrackerRepositoryProvider;
  }

  @Override
  public SearchSavedJobsUseCase get() {
    return provideSearchSavedJobsUseCase(jobTrackerRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideSearchSavedJobsUseCaseFactory create(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    return new UseCaseModule_ProvideSearchSavedJobsUseCaseFactory(jobTrackerRepositoryProvider);
  }

  public static SearchSavedJobsUseCase provideSearchSavedJobsUseCase(
      JobTrackerRepository jobTrackerRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideSearchSavedJobsUseCase(jobTrackerRepository));
  }
}
