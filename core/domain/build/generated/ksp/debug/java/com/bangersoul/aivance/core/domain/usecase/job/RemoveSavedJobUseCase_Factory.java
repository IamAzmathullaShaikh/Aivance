package com.bangersoul.aivance.core.domain.usecase.job;

import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository;
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
public final class RemoveSavedJobUseCase_Factory implements Factory<RemoveSavedJobUseCase> {
  private final Provider<JobTrackerRepository> jobTrackerRepositoryProvider;

  private RemoveSavedJobUseCase_Factory(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    this.jobTrackerRepositoryProvider = jobTrackerRepositoryProvider;
  }

  @Override
  public RemoveSavedJobUseCase get() {
    return newInstance(jobTrackerRepositoryProvider.get());
  }

  public static RemoveSavedJobUseCase_Factory create(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    return new RemoveSavedJobUseCase_Factory(jobTrackerRepositoryProvider);
  }

  public static RemoveSavedJobUseCase newInstance(JobTrackerRepository jobTrackerRepository) {
    return new RemoveSavedJobUseCase(jobTrackerRepository);
  }
}
