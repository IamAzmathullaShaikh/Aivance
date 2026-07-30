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
public final class SaveJobUseCase_Factory implements Factory<SaveJobUseCase> {
  private final Provider<JobTrackerRepository> jobTrackerRepositoryProvider;

  private SaveJobUseCase_Factory(Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    this.jobTrackerRepositoryProvider = jobTrackerRepositoryProvider;
  }

  @Override
  public SaveJobUseCase get() {
    return newInstance(jobTrackerRepositoryProvider.get());
  }

  public static SaveJobUseCase_Factory create(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    return new SaveJobUseCase_Factory(jobTrackerRepositoryProvider);
  }

  public static SaveJobUseCase newInstance(JobTrackerRepository jobTrackerRepository) {
    return new SaveJobUseCase(jobTrackerRepository);
  }
}
