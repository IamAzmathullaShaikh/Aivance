package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.InterviewRepository;
import com.bangersoul.aivance.core.domain.usecase.interview.GenerateFeedbackUseCase;
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
public final class UseCaseModule_ProvideGenerateFeedbackUseCaseFactory implements Factory<GenerateFeedbackUseCase> {
  private final Provider<InterviewRepository> interviewRepositoryProvider;

  private UseCaseModule_ProvideGenerateFeedbackUseCaseFactory(
      Provider<InterviewRepository> interviewRepositoryProvider) {
    this.interviewRepositoryProvider = interviewRepositoryProvider;
  }

  @Override
  public GenerateFeedbackUseCase get() {
    return provideGenerateFeedbackUseCase(interviewRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideGenerateFeedbackUseCaseFactory create(
      Provider<InterviewRepository> interviewRepositoryProvider) {
    return new UseCaseModule_ProvideGenerateFeedbackUseCaseFactory(interviewRepositoryProvider);
  }

  public static GenerateFeedbackUseCase provideGenerateFeedbackUseCase(
      InterviewRepository interviewRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideGenerateFeedbackUseCase(interviewRepository));
  }
}
