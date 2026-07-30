package com.bangersoul.aivance.core.domain.usecase.interview;

import com.bangersoul.aivance.core.domain.repository.AiRepository;
import com.bangersoul.aivance.core.domain.repository.InterviewRepository;
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
public final class EvaluateAnswersUseCase_Factory implements Factory<EvaluateAnswersUseCase> {
  private final Provider<InterviewRepository> interviewRepositoryProvider;

  private final Provider<AiRepository> aiRepositoryProvider;

  private EvaluateAnswersUseCase_Factory(Provider<InterviewRepository> interviewRepositoryProvider,
      Provider<AiRepository> aiRepositoryProvider) {
    this.interviewRepositoryProvider = interviewRepositoryProvider;
    this.aiRepositoryProvider = aiRepositoryProvider;
  }

  @Override
  public EvaluateAnswersUseCase get() {
    return newInstance(interviewRepositoryProvider.get(), aiRepositoryProvider.get());
  }

  public static EvaluateAnswersUseCase_Factory create(
      Provider<InterviewRepository> interviewRepositoryProvider,
      Provider<AiRepository> aiRepositoryProvider) {
    return new EvaluateAnswersUseCase_Factory(interviewRepositoryProvider, aiRepositoryProvider);
  }

  public static EvaluateAnswersUseCase newInstance(InterviewRepository interviewRepository,
      AiRepository aiRepository) {
    return new EvaluateAnswersUseCase(interviewRepository, aiRepository);
  }
}
