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
public final class SuggestLearningPathUseCase_Factory implements Factory<SuggestLearningPathUseCase> {
  private final Provider<AiRepository> aiRepositoryProvider;

  private SuggestLearningPathUseCase_Factory(Provider<AiRepository> aiRepositoryProvider) {
    this.aiRepositoryProvider = aiRepositoryProvider;
  }

  @Override
  public SuggestLearningPathUseCase get() {
    return newInstance(aiRepositoryProvider.get());
  }

  public static SuggestLearningPathUseCase_Factory create(
      Provider<AiRepository> aiRepositoryProvider) {
    return new SuggestLearningPathUseCase_Factory(aiRepositoryProvider);
  }

  public static SuggestLearningPathUseCase newInstance(AiRepository aiRepository) {
    return new SuggestLearningPathUseCase(aiRepository);
  }
}
