package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.AiRepository;
import com.bangersoul.aivance.core.domain.usecase.ai.RegenerateResponseUseCase;
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
public final class UseCaseModule_ProvideRegenerateResponseUseCaseFactory implements Factory<RegenerateResponseUseCase> {
  private final Provider<AiRepository> aiRepositoryProvider;

  private UseCaseModule_ProvideRegenerateResponseUseCaseFactory(
      Provider<AiRepository> aiRepositoryProvider) {
    this.aiRepositoryProvider = aiRepositoryProvider;
  }

  @Override
  public RegenerateResponseUseCase get() {
    return provideRegenerateResponseUseCase(aiRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideRegenerateResponseUseCaseFactory create(
      Provider<AiRepository> aiRepositoryProvider) {
    return new UseCaseModule_ProvideRegenerateResponseUseCaseFactory(aiRepositoryProvider);
  }

  public static RegenerateResponseUseCase provideRegenerateResponseUseCase(
      AiRepository aiRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideRegenerateResponseUseCase(aiRepository));
  }
}
