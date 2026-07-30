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
public final class EndInterviewUseCase_Factory implements Factory<EndInterviewUseCase> {
  private final Provider<InterviewRepository> interviewRepositoryProvider;

  private EndInterviewUseCase_Factory(Provider<InterviewRepository> interviewRepositoryProvider) {
    this.interviewRepositoryProvider = interviewRepositoryProvider;
  }

  @Override
  public EndInterviewUseCase get() {
    return newInstance(interviewRepositoryProvider.get());
  }

  public static EndInterviewUseCase_Factory create(
      Provider<InterviewRepository> interviewRepositoryProvider) {
    return new EndInterviewUseCase_Factory(interviewRepositoryProvider);
  }

  public static EndInterviewUseCase newInstance(InterviewRepository interviewRepository) {
    return new EndInterviewUseCase(interviewRepository);
  }
}
