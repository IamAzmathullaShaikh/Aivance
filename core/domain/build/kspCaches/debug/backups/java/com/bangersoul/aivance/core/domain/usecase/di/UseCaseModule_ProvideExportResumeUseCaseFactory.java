package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.ResumeRepository;
import com.bangersoul.aivance.core.domain.usecase.resume.ExportResumeUseCase;
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
public final class UseCaseModule_ProvideExportResumeUseCaseFactory implements Factory<ExportResumeUseCase> {
  private final Provider<ResumeRepository> resumeRepositoryProvider;

  private UseCaseModule_ProvideExportResumeUseCaseFactory(
      Provider<ResumeRepository> resumeRepositoryProvider) {
    this.resumeRepositoryProvider = resumeRepositoryProvider;
  }

  @Override
  public ExportResumeUseCase get() {
    return provideExportResumeUseCase(resumeRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideExportResumeUseCaseFactory create(
      Provider<ResumeRepository> resumeRepositoryProvider) {
    return new UseCaseModule_ProvideExportResumeUseCaseFactory(resumeRepositoryProvider);
  }

  public static ExportResumeUseCase provideExportResumeUseCase(ResumeRepository resumeRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideExportResumeUseCase(resumeRepository));
  }
}
