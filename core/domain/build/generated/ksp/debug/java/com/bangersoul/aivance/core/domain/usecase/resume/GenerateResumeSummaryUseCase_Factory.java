package com.bangersoul.aivance.core.domain.usecase.resume;

import com.bangersoul.aivance.core.domain.repository.ResumeRepository;
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
public final class GenerateResumeSummaryUseCase_Factory implements Factory<GenerateResumeSummaryUseCase> {
  private final Provider<ResumeRepository> resumeRepositoryProvider;

  private GenerateResumeSummaryUseCase_Factory(
      Provider<ResumeRepository> resumeRepositoryProvider) {
    this.resumeRepositoryProvider = resumeRepositoryProvider;
  }

  @Override
  public GenerateResumeSummaryUseCase get() {
    return newInstance(resumeRepositoryProvider.get());
  }

  public static GenerateResumeSummaryUseCase_Factory create(
      Provider<ResumeRepository> resumeRepositoryProvider) {
    return new GenerateResumeSummaryUseCase_Factory(resumeRepositoryProvider);
  }

  public static GenerateResumeSummaryUseCase newInstance(ResumeRepository resumeRepository) {
    return new GenerateResumeSummaryUseCase(resumeRepository);
  }
}
