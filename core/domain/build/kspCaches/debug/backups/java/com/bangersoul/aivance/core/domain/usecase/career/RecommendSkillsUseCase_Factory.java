package com.bangersoul.aivance.core.domain.usecase.career;

import com.bangersoul.aivance.core.domain.repository.AiRepository;
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
public final class RecommendSkillsUseCase_Factory implements Factory<RecommendSkillsUseCase> {
  private final Provider<AiRepository> aiRepositoryProvider;

  private RecommendSkillsUseCase_Factory(Provider<AiRepository> aiRepositoryProvider) {
    this.aiRepositoryProvider = aiRepositoryProvider;
  }

  @Override
  public RecommendSkillsUseCase get() {
    return newInstance(aiRepositoryProvider.get());
  }

  public static RecommendSkillsUseCase_Factory create(Provider<AiRepository> aiRepositoryProvider) {
    return new RecommendSkillsUseCase_Factory(aiRepositoryProvider);
  }

  public static RecommendSkillsUseCase newInstance(AiRepository aiRepository) {
    return new RecommendSkillsUseCase(aiRepository);
  }
}
