package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository;
import com.bangersoul.aivance.core.domain.usecase.job.SaveJobUseCase;
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
public final class UseCaseModule_ProvideSaveJobUseCaseFactory implements Factory<SaveJobUseCase> {
  private final Provider<JobTrackerRepository> jobTrackerRepositoryProvider;

  private UseCaseModule_ProvideSaveJobUseCaseFactory(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    this.jobTrackerRepositoryProvider = jobTrackerRepositoryProvider;
  }

  @Override
  public SaveJobUseCase get() {
    return provideSaveJobUseCase(jobTrackerRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideSaveJobUseCaseFactory create(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    return new UseCaseModule_ProvideSaveJobUseCaseFactory(jobTrackerRepositoryProvider);
  }

  public static SaveJobUseCase provideSaveJobUseCase(JobTrackerRepository jobTrackerRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideSaveJobUseCase(jobTrackerRepository));
  }
}
