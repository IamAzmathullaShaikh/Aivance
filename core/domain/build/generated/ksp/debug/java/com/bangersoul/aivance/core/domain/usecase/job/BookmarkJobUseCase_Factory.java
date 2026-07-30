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
public final class BookmarkJobUseCase_Factory implements Factory<BookmarkJobUseCase> {
  private final Provider<JobTrackerRepository> jobTrackerRepositoryProvider;

  private BookmarkJobUseCase_Factory(Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    this.jobTrackerRepositoryProvider = jobTrackerRepositoryProvider;
  }

  @Override
  public BookmarkJobUseCase get() {
    return newInstance(jobTrackerRepositoryProvider.get());
  }

  public static BookmarkJobUseCase_Factory create(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    return new BookmarkJobUseCase_Factory(jobTrackerRepositoryProvider);
  }

  public static BookmarkJobUseCase newInstance(JobTrackerRepository jobTrackerRepository) {
    return new BookmarkJobUseCase(jobTrackerRepository);
  }
}
