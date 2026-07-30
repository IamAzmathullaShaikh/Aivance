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
public final class ExportResumeUseCase_Factory implements Factory<ExportResumeUseCase> {
  private final Provider<ResumeRepository> resumeRepositoryProvider;

  private ExportResumeUseCase_Factory(Provider<ResumeRepository> resumeRepositoryProvider) {
    this.resumeRepositoryProvider = resumeRepositoryProvider;
  }

  @Override
  public ExportResumeUseCase get() {
    return newInstance(resumeRepositoryProvider.get());
  }

  public static ExportResumeUseCase_Factory create(
      Provider<ResumeRepository> resumeRepositoryProvider) {
    return new ExportResumeUseCase_Factory(resumeRepositoryProvider);
  }

  public static ExportResumeUseCase newInstance(ResumeRepository resumeRepository) {
    return new ExportResumeUseCase(resumeRepository);
  }
}
