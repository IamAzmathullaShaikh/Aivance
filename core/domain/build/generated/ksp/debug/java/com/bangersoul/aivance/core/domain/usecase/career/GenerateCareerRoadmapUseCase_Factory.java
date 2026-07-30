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
public final class GenerateCareerRoadmapUseCase_Factory implements Factory<GenerateCareerRoadmapUseCase> {
  private final Provider<AiRepository> aiRepositoryProvider;

  private GenerateCareerRoadmapUseCase_Factory(Provider<AiRepository> aiRepositoryProvider) {
    this.aiRepositoryProvider = aiRepositoryProvider;
  }

  @Override
  public GenerateCareerRoadmapUseCase get() {
    return newInstance(aiRepositoryProvider.get());
  }

  public static GenerateCareerRoadmapUseCase_Factory create(
      Provider<AiRepository> aiRepositoryProvider) {
    return new GenerateCareerRoadmapUseCase_Factory(aiRepositoryProvider);
  }

  public static GenerateCareerRoadmapUseCase newInstance(AiRepository aiRepository) {
    return new GenerateCareerRoadmapUseCase(aiRepository);
  }
}
