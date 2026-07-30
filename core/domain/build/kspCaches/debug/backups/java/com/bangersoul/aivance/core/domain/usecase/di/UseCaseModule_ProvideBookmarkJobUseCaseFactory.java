package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.JobTrackerRepository;
import com.bangersoul.aivance.core.domain.usecase.job.BookmarkJobUseCase;
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
public final class UseCaseModule_ProvideBookmarkJobUseCaseFactory implements Factory<BookmarkJobUseCase> {
  private final Provider<JobTrackerRepository> jobTrackerRepositoryProvider;

  private UseCaseModule_ProvideBookmarkJobUseCaseFactory(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    this.jobTrackerRepositoryProvider = jobTrackerRepositoryProvider;
  }

  @Override
  public BookmarkJobUseCase get() {
    return provideBookmarkJobUseCase(jobTrackerRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideBookmarkJobUseCaseFactory create(
      Provider<JobTrackerRepository> jobTrackerRepositoryProvider) {
    return new UseCaseModule_ProvideBookmarkJobUseCaseFactory(jobTrackerRepositoryProvider);
  }

  public static BookmarkJobUseCase provideBookmarkJobUseCase(
      JobTrackerRepository jobTrackerRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideBookmarkJobUseCase(jobTrackerRepository));
  }
}
