package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.AiRepository;
import com.bangersoul.aivance.core.domain.repository.InterviewRepository;
import com.bangersoul.aivance.core.domain.usecase.interview.EvaluateAnswersUseCase;
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
public final class UseCaseModule_ProvideEvaluateAnswersUseCaseFactory implements Factory<EvaluateAnswersUseCase> {
  private final Provider<InterviewRepository> interviewRepositoryProvider;

  private final Provider<AiRepository> aiRepositoryProvider;

  private UseCaseModule_ProvideEvaluateAnswersUseCaseFactory(
      Provider<InterviewRepository> interviewRepositoryProvider,
      Provider<AiRepository> aiRepositoryProvider) {
    this.interviewRepositoryProvider = interviewRepositoryProvider;
    this.aiRepositoryProvider = aiRepositoryProvider;
  }

  @Override
  public EvaluateAnswersUseCase get() {
    return provideEvaluateAnswersUseCase(interviewRepositoryProvider.get(), aiRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideEvaluateAnswersUseCaseFactory create(
      Provider<InterviewRepository> interviewRepositoryProvider,
      Provider<AiRepository> aiRepositoryProvider) {
    return new UseCaseModule_ProvideEvaluateAnswersUseCaseFactory(interviewRepositoryProvider, aiRepositoryProvider);
  }

  public static EvaluateAnswersUseCase provideEvaluateAnswersUseCase(
      InterviewRepository interviewRepository, AiRepository aiRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideEvaluateAnswersUseCase(interviewRepository, aiRepository));
  }
}
