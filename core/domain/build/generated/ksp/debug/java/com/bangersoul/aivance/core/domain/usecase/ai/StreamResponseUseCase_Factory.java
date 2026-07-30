package com.bangersoul.aivance.core.domain.usecase.ai;

import com.bangersoul.aivance.core.domain.repository.AiRepository;
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager;
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
public final class StreamResponseUseCase_Factory implements Factory<StreamResponseUseCase> {
  private final Provider<AiRepository> aiRepositoryProvider;

  private final Provider<ProviderManager> providerManagerProvider;

  private StreamResponseUseCase_Factory(Provider<AiRepository> aiRepositoryProvider,
      Provider<ProviderManager> providerManagerProvider) {
    this.aiRepositoryProvider = aiRepositoryProvider;
    this.providerManagerProvider = providerManagerProvider;
  }

  @Override
  public StreamResponseUseCase get() {
    return newInstance(aiRepositoryProvider.get(), providerManagerProvider.get());
  }

  public static StreamResponseUseCase_Factory create(Provider<AiRepository> aiRepositoryProvider,
      Provider<ProviderManager> providerManagerProvider) {
    return new StreamResponseUseCase_Factory(aiRepositoryProvider, providerManagerProvider);
  }

  public static StreamResponseUseCase newInstance(AiRepository aiRepository,
      ProviderManager providerManager) {
    return new StreamResponseUseCase(aiRepository, providerManager);
  }
}
