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
public final class ParseResumeUseCase_Factory implements Factory<ParseResumeUseCase> {
  private final Provider<ResumeRepository> resumeRepositoryProvider;

  private ParseResumeUseCase_Factory(Provider<ResumeRepository> resumeRepositoryProvider) {
    this.resumeRepositoryProvider = resumeRepositoryProvider;
  }

  @Override
  public ParseResumeUseCase get() {
    return newInstance(resumeRepositoryProvider.get());
  }

  public static ParseResumeUseCase_Factory create(
      Provider<ResumeRepository> resumeRepositoryProvider) {
    return new ParseResumeUseCase_Factory(resumeRepositoryProvider);
  }

  public static ParseResumeUseCase newInstance(ResumeRepository resumeRepository) {
    return new ParseResumeUseCase(resumeRepository);
  }
}
