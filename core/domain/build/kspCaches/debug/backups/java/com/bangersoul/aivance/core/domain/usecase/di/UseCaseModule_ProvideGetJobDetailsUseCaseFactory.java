package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.JobRepository;
import com.bangersoul.aivance.core.domain.usecase.job.GetJobDetailsUseCase;
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
public final class UseCaseModule_ProvideGetJobDetailsUseCaseFactory implements Factory<GetJobDetailsUseCase> {
  private final Provider<JobRepository> jobRepositoryProvider;

  private UseCaseModule_ProvideGetJobDetailsUseCaseFactory(
      Provider<JobRepository> jobRepositoryProvider) {
    this.jobRepositoryProvider = jobRepositoryProvider;
  }

  @Override
  public GetJobDetailsUseCase get() {
    return provideGetJobDetailsUseCase(jobRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideGetJobDetailsUseCaseFactory create(
      Provider<JobRepository> jobRepositoryProvider) {
    return new UseCaseModule_ProvideGetJobDetailsUseCaseFactory(jobRepositoryProvider);
  }

  public static GetJobDetailsUseCase provideGetJobDetailsUseCase(JobRepository jobRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideGetJobDetailsUseCase(jobRepository));
  }
}
