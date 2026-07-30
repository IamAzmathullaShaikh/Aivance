package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.AiRepository;
import com.bangersoul.aivance.core.domain.usecase.ai.StreamResponseUseCase;
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager;
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
public final class UseCaseModule_ProvideStreamResponseUseCaseFactory implements Factory<StreamResponseUseCase> {
  private final Provider<AiRepository> aiRepositoryProvider;

  private final Provider<ProviderManager> providerManagerProvider;

  private UseCaseModule_ProvideStreamResponseUseCaseFactory(
      Provider<AiRepository> aiRepositoryProvider,
      Provider<ProviderManager> providerManagerProvider) {
    this.aiRepositoryProvider = aiRepositoryProvider;
    this.providerManagerProvider = providerManagerProvider;
  }

  @Override
  public StreamResponseUseCase get() {
    return provideStreamResponseUseCase(aiRepositoryProvider.get(), providerManagerProvider.get());
  }

  public static UseCaseModule_ProvideStreamResponseUseCaseFactory create(
      Provider<AiRepository> aiRepositoryProvider,
      Provider<ProviderManager> providerManagerProvider) {
    return new UseCaseModule_ProvideStreamResponseUseCaseFactory(aiRepositoryProvider, providerManagerProvider);
  }

  public static StreamResponseUseCase provideStreamResponseUseCase(AiRepository aiRepository,
      ProviderManager providerManager) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideStreamResponseUseCase(aiRepository, providerManager));
  }
}
