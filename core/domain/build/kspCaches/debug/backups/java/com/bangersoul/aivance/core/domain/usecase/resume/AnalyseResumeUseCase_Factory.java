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
public final class AnalyseResumeUseCase_Factory implements Factory<AnalyseResumeUseCase> {
  private final Provider<ResumeRepository> resumeRepositoryProvider;

  private AnalyseResumeUseCase_Factory(Provider<ResumeRepository> resumeRepositoryProvider) {
    this.resumeRepositoryProvider = resumeRepositoryProvider;
  }

  @Override
  public AnalyseResumeUseCase get() {
    return newInstance(resumeRepositoryProvider.get());
  }

  public static AnalyseResumeUseCase_Factory create(
      Provider<ResumeRepository> resumeRepositoryProvider) {
    return new AnalyseResumeUseCase_Factory(resumeRepositoryProvider);
  }

  public static AnalyseResumeUseCase newInstance(ResumeRepository resumeRepository) {
    return new AnalyseResumeUseCase(resumeRepository);
  }
}
