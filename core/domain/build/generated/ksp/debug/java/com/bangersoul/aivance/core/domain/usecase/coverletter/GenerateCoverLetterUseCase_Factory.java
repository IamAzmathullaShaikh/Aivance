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
public final class GenerateCoverLetterUseCase_Factory implements Factory<GenerateCoverLetterUseCase> {
  private final Provider<CoverLetterRepository> coverLetterRepositoryProvider;

  private GenerateCoverLetterUseCase_Factory(
      Provider<CoverLetterRepository> coverLetterRepositoryProvider) {
    this.coverLetterRepositoryProvider = coverLetterRepositoryProvider;
  }

  @Override
  public GenerateCoverLetterUseCase get() {
    return newInstance(coverLetterRepositoryProvider.get());
  }

  public static GenerateCoverLetterUseCase_Factory create(
      Provider<CoverLetterRepository> coverLetterRepositoryProvider) {
    return new GenerateCoverLetterUseCase_Factory(coverLetterRepositoryProvider);
  }

  public static GenerateCoverLetterUseCase newInstance(
      CoverLetterRepository coverLetterRepository) {
    return new GenerateCoverLetterUseCase(coverLetterRepository);
  }
}
