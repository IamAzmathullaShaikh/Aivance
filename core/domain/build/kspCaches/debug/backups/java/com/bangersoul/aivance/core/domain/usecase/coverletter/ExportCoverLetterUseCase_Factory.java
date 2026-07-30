package com.bangersoul.aivance.core.domain.usecase.coverletter;

import com.bangersoul.aivance.core.domain.repository.CoverLetterRepository;
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
public final class ExportCoverLetterUseCase_Factory implements Factory<ExportCoverLetterUseCase> {
  private final Provider<CoverLetterRepository> coverLetterRepositoryProvider;

  private ExportCoverLetterUseCase_Factory(
      Provider<CoverLetterRepository> coverLetterRepositoryProvider) {
    this.coverLetterRepositoryProvider = coverLetterRepositoryProvider;
  }

  @Override
  public ExportCoverLetterUseCase get() {
    return newInstance(coverLetterRepositoryProvider.get());
  }

  public static ExportCoverLetterUseCase_Factory create(
      Provider<CoverLetterRepository> coverLetterRepositoryProvider) {
    return new ExportCoverLetterUseCase_Factory(coverLetterRepositoryProvider);
  }

  public static ExportCoverLetterUseCase newInstance(CoverLetterRepository coverLetterRepository) {
    return new ExportCoverLetterUseCase(coverLetterRepository);
  }
}
