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
public final class ImproveCoverLetterUseCase_Factory implements Factory<ImproveCoverLetterUseCase> {
  private final Provider<CoverLetterRepository> coverLetterRepositoryProvider;

  private ImproveCoverLetterUseCase_Factory(
      Provider<CoverLetterRepository> coverLetterRepositoryProvider) {
    this.coverLetterRepositoryProvider = coverLetterRepositoryProvider;
  }

  @Override
  public ImproveCoverLetterUseCase get() {
    return newInstance(coverLetterRepositoryProvider.get());
  }

  public static ImproveCoverLetterUseCase_Factory create(
      Provider<CoverLetterRepository> coverLetterRepositoryProvider) {
    return new ImproveCoverLetterUseCase_Factory(coverLetterRepositoryProvider);
  }

  public static ImproveCoverLetterUseCase newInstance(CoverLetterRepository coverLetterRepository) {
    return new ImproveCoverLetterUseCase(coverLetterRepository);
  }
}
