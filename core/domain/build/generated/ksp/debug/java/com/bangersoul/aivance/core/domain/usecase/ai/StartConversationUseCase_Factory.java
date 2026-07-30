package com.bangersoul.aivance.core.domain.usecase.ai;

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
public final class StartConversationUseCase_Factory implements Factory<StartConversationUseCase> {
  private final Provider<AiRepository> aiRepositoryProvider;

  private StartConversationUseCase_Factory(Provider<AiRepository> aiRepositoryProvider) {
    this.aiRepositoryProvider = aiRepositoryProvider;
  }

  @Override
  public StartConversationUseCase get() {
    return newInstance(aiRepositoryProvider.get());
  }

  public static StartConversationUseCase_Factory create(
      Provider<AiRepository> aiRepositoryProvider) {
    return new StartConversationUseCase_Factory(aiRepositoryProvider);
  }

  public static StartConversationUseCase newInstance(AiRepository aiRepository) {
    return new StartConversationUseCase(aiRepository);
  }
}
