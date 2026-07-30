package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository;
import com.bangersoul.aivance.core.domain.usecase.job.ApplyToJobUseCase;
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
public final class UseCaseModule_ProvideApplyToJobUseCaseFactory implements Factory<ApplyToJobUseCase> {
  private final Provider<JobTrackerRepository> jobTrackerRepositoryProvider;

  private UseCaseModule_ProvideApplyToJobUseCaseFactory(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    this.jobTrackerRepositoryProvider = jobTrackerRepositoryProvider;
  }

  @Override
  public ApplyToJobUseCase get() {
    return provideApplyToJobUseCase(jobTrackerRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideApplyToJobUseCaseFactory create(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    return new UseCaseModule_ProvideApplyToJobUseCaseFactory(jobTrackerRepositoryProvider);
  }

  public static ApplyToJobUseCase provideApplyToJobUseCase(
      JobTrackerRepository jobTrackerRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideApplyToJobUseCase(jobTrackerRepository));
  }
}
