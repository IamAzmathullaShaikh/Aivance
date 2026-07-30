package com.bangersoul.aivance.core.domain.usecase.interview;

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
public final class GenerateInterviewQuestionsUseCase_Factory implements Factory<GenerateInterviewQuestionsUseCase> {
  private final Provider<AiRepository> aiRepositoryProvider;

  private GenerateInterviewQuestionsUseCase_Factory(Provider<AiRepository> aiRepositoryProvider) {
    this.aiRepositoryProvider = aiRepositoryProvider;
  }

  @Override
  public GenerateInterviewQuestionsUseCase get() {
    return newInstance(aiRepositoryProvider.get());
  }

  public static GenerateInterviewQuestionsUseCase_Factory create(
      Provider<AiRepository> aiRepositoryProvider) {
    return new GenerateInterviewQuestionsUseCase_Factory(aiRepositoryProvider);
  }

  public static GenerateInterviewQuestionsUseCase newInstance(AiRepository aiRepository) {
    return new GenerateInterviewQuestionsUseCase(aiRepository);
  }
}
