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
public final class SendMessageUseCase_Factory implements Factory<SendMessageUseCase> {
  private final Provider<AiRepository> aiRepositoryProvider;

  private SendMessageUseCase_Factory(Provider<AiRepository> aiRepositoryProvider) {
    this.aiRepositoryProvider = aiRepositoryProvider;
  }

  @Override
  public SendMessageUseCase get() {
    return newInstance(aiRepositoryProvider.get());
  }

  public static SendMessageUseCase_Factory create(Provider<AiRepository> aiRepositoryProvider) {
    return new SendMessageUseCase_Factory(aiRepositoryProvider);
  }

  public static SendMessageUseCase newInstance(AiRepository aiRepository) {
    return new SendMessageUseCase(aiRepository);
  }
}
