package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.AiRepository;
import com.bangersoul.aivance.core.domain.usecase.ai.StartConversationUseCase;
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
public final class UseCaseModule_ProvideStartConversationUseCaseFactory implements Factory<StartConversationUseCase> {
  private final Provider<AiRepository> aiRepositoryProvider;

  private UseCaseModule_ProvideStartConversationUseCaseFactory(
      Provider<AiRepository> aiRepositoryProvider) {
    this.aiRepositoryProvider = aiRepositoryProvider;
  }

  @Override
  public StartConversationUseCase get() {
    return provideStartConversationUseCase(aiRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideStartConversationUseCaseFactory create(
      Provider<AiRepository> aiRepositoryProvider) {
    return new UseCaseModule_ProvideStartConversationUseCaseFactory(aiRepositoryProvider);
  }

  public static StartConversationUseCase provideStartConversationUseCase(
      AiRepository aiRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideStartConversationUseCase(aiRepository));
  }
}
