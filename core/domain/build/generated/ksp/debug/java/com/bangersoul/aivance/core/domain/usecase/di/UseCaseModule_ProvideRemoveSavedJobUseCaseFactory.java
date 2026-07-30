package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository;
import com.bangersoul.aivance.core.domain.usecase.job.RemoveSavedJobUseCase;
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
public final class UseCaseModule_ProvideRemoveSavedJobUseCaseFactory implements Factory<RemoveSavedJobUseCase> {
  private final Provider<JobTrackerRepository> jobTrackerRepositoryProvider;

  private UseCaseModule_ProvideRemoveSavedJobUseCaseFactory(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    this.jobTrackerRepositoryProvider = jobTrackerRepositoryProvider;
  }

  @Override
  public RemoveSavedJobUseCase get() {
    return provideRemoveSavedJobUseCase(jobTrackerRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideRemoveSavedJobUseCaseFactory create(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    return new UseCaseModule_ProvideRemoveSavedJobUseCaseFactory(jobTrackerRepositoryProvider);
  }

  public static RemoveSavedJobUseCase provideRemoveSavedJobUseCase(
      JobTrackerRepository jobTrackerRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideRemoveSavedJobUseCase(jobTrackerRepository));
  }
}
