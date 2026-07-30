package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.AiRepository;
import com.bangersoul.aivance.core.domain.usecase.career.RecommendSkillsUseCase;
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
public final class UseCaseModule_ProvideRecommendSkillsUseCaseFactory implements Factory<RecommendSkillsUseCase> {
  private final Provider<AiRepository> aiRepositoryProvider;

  private UseCaseModule_ProvideRecommendSkillsUseCaseFactory(
      Provider<AiRepository> aiRepositoryProvider) {
    this.aiRepositoryProvider = aiRepositoryProvider;
  }

  @Override
  public RecommendSkillsUseCase get() {
    return provideRecommendSkillsUseCase(aiRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideRecommendSkillsUseCaseFactory create(
      Provider<AiRepository> aiRepositoryProvider) {
    return new UseCaseModule_ProvideRecommendSkillsUseCaseFactory(aiRepositoryProvider);
  }

  public static RecommendSkillsUseCase provideRecommendSkillsUseCase(AiRepository aiRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideRecommendSkillsUseCase(aiRepository));
  }
}
