package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.AiRepository;
import com.bangersoul.aivance.core.domain.usecase.ai.SummariseConversationUseCase;
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
public final class UseCaseModule_ProvideSummariseConversationUseCaseFactory implements Factory<SummariseConversationUseCase> {
  private final Provider<AiRepository> aiRepositoryProvider;

  private UseCaseModule_ProvideSummariseConversationUseCaseFactory(
      Provider<AiRepository> aiRepositoryProvider) {
    this.aiRepositoryProvider = aiRepositoryProvider;
  }

  @Override
  public SummariseConversationUseCase get() {
    return provideSummariseConversationUseCase(aiRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideSummariseConversationUseCaseFactory create(
      Provider<AiRepository> aiRepositoryProvider) {
    return new UseCaseModule_ProvideSummariseConversationUseCaseFactory(aiRepositoryProvider);
  }

  public static SummariseConversationUseCase provideSummariseConversationUseCase(
      AiRepository aiRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideSummariseConversationUseCase(aiRepository));
  }
}
