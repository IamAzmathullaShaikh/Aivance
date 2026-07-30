package com.bangersoul.aivance.core.domain.usecase.interview;

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
public final class GenerateFeedbackUseCase_Factory implements Factory<GenerateFeedbackUseCase> {
  private final Provider<InterviewRepository> interviewRepositoryProvider;

  private GenerateFeedbackUseCase_Factory(
      Provider<InterviewRepository> interviewRepositoryProvider) {
    this.interviewRepositoryProvider = interviewRepositoryProvider;
  }

  @Override
  public GenerateFeedbackUseCase get() {
    return newInstance(interviewRepositoryProvider.get());
  }

  public static GenerateFeedbackUseCase_Factory create(
      Provider<InterviewRepository> interviewRepositoryProvider) {
    return new GenerateFeedbackUseCase_Factory(interviewRepositoryProvider);
  }

  public static GenerateFeedbackUseCase newInstance(InterviewRepository interviewRepository) {
    return new GenerateFeedbackUseCase(interviewRepository);
  }
}
