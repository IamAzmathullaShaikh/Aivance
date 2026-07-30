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
public final class StartInterviewSessionUseCase_Factory implements Factory<StartInterviewSessionUseCase> {
  private final Provider<InterviewRepository> interviewRepositoryProvider;

  private StartInterviewSessionUseCase_Factory(
      Provider<InterviewRepository> interviewRepositoryProvider) {
    this.interviewRepositoryProvider = interviewRepositoryProvider;
  }

  @Override
  public StartInterviewSessionUseCase get() {
    return newInstance(interviewRepositoryProvider.get());
  }

  public static StartInterviewSessionUseCase_Factory create(
      Provider<InterviewRepository> interviewRepositoryProvider) {
    return new StartInterviewSessionUseCase_Factory(interviewRepositoryProvider);
  }

  public static StartInterviewSessionUseCase newInstance(InterviewRepository interviewRepository) {
    return new StartInterviewSessionUseCase(interviewRepository);
  }
}
