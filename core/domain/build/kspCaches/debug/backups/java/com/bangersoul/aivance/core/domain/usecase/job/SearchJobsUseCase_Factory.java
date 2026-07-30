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
public final class SearchJobsUseCase_Factory implements Factory<SearchJobsUseCase> {
  private final Provider<JobRepository> jobRepositoryProvider;

  private SearchJobsUseCase_Factory(Provider<JobRepository> jobRepositoryProvider) {
    this.jobRepositoryProvider = jobRepositoryProvider;
  }

  @Override
  public SearchJobsUseCase get() {
    return newInstance(jobRepositoryProvider.get());
  }

  public static SearchJobsUseCase_Factory create(Provider<JobRepository> jobRepositoryProvider) {
    return new SearchJobsUseCase_Factory(jobRepositoryProvider);
  }

  public static SearchJobsUseCase newInstance(JobRepository jobRepository) {
    return new SearchJobsUseCase(jobRepository);
  }
}
