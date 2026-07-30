package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.ResumeRepository;
import com.bangersoul.aivance.core.domain.usecase.resume.ParseResumeUseCase;
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
public final class UseCaseModule_ProvideParseResumeUseCaseFactory implements Factory<ParseResumeUseCase> {
  private final Provider<ResumeRepository> resumeRepositoryProvider;

  private UseCaseModule_ProvideParseResumeUseCaseFactory(
      Provider<ResumeRepository> resumeRepositoryProvider) {
    this.resumeRepositoryProvider = resumeRepositoryProvider;
  }

  @Override
  public ParseResumeUseCase get() {
    return provideParseResumeUseCase(resumeRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideParseResumeUseCaseFactory create(
      Provider<ResumeRepository> resumeRepositoryProvider) {
    return new UseCaseModule_ProvideParseResumeUseCaseFactory(resumeRepositoryProvider);
  }

  public static ParseResumeUseCase provideParseResumeUseCase(ResumeRepository resumeRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideParseResumeUseCase(resumeRepository));
  }
}
