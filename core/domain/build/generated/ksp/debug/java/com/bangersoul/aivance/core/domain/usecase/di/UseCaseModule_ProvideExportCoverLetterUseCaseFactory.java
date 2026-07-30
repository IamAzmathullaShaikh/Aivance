package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository;
import com.bangersoul.aivance.core.domain.usecase.coverletter.ExportCoverLetterUseCase;
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
public final class UseCaseModule_ProvideExportCoverLetterUseCaseFactory implements Factory<ExportCoverLetterUseCase> {
  private final Provider<CoverLetterRepository> coverLetterRepositoryProvider;

  private UseCaseModule_ProvideExportCoverLetterUseCaseFactory(
      Provider<CoverLetterRepository> coverLetterRepositoryProvider) {
    this.coverLetterRepositoryProvider = coverLetterRepositoryProvider;
  }

  @Override
  public ExportCoverLetterUseCase get() {
    return provideExportCoverLetterUseCase(coverLetterRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideExportCoverLetterUseCaseFactory create(
      Provider<CoverLetterRepository> coverLetterRepositoryProvider) {
    return new UseCaseModule_ProvideExportCoverLetterUseCaseFactory(coverLetterRepositoryProvider);
  }

  public static ExportCoverLetterUseCase provideExportCoverLetterUseCase(
      CoverLetterRepository coverLetterRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideExportCoverLetterUseCase(coverLetterRepository));
  }
}
