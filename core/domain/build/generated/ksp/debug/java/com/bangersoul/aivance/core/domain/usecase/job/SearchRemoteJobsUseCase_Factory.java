package com.bangersoul.aivance.core.domain.usecase.job;

import com.bangersoul.aivance.core.domain.repository.JobRepository;
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
public final class SearchRemoteJobsUseCase_Factory implements Factory<SearchRemoteJobsUseCase> {
  private final Provider<JobRepository> jobRepositoryProvider;

  private SearchRemoteJobsUseCase_Factory(Provider<JobRepository> jobRepositoryProvider) {
    this.jobRepositoryProvider = jobRepositoryProvider;
  }

  @Override
  public SearchRemoteJobsUseCase get() {
    return newInstance(jobRepositoryProvider.get());
  }

  public static SearchRemoteJobsUseCase_Factory create(
      Provider<JobRepository> jobRepositoryProvider) {
    return new SearchRemoteJobsUseCase_Factory(jobRepositoryProvider);
  }

  public static SearchRemoteJobsUseCase newInstance(JobRepository jobRepository) {
    return new SearchRemoteJobsUseCase(jobRepository);
  }
}
