package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.usecase.provider.GetAvailableModelsUseCase;
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
public final class UseCaseModule_ProvideGetAvailableModelsUseCaseFactory implements Factory<GetAvailableModelsUseCase> {
  private final Provider<ProviderManager> providerManagerProvider;

  private UseCaseModule_ProvideGetAvailableModelsUseCaseFactory(
      Provider<ProviderManager> providerManagerProvider) {
    this.providerManagerProvider = providerManagerProvider;
  }

  @Override
  public GetAvailableModelsUseCase get() {
    return provideGetAvailableModelsUseCase(providerManagerProvider.get());
  }

  public static UseCaseModule_ProvideGetAvailableModelsUseCaseFactory create(
      Provider<ProviderManager> providerManagerProvider) {
    return new UseCaseModule_ProvideGetAvailableModelsUseCaseFactory(providerManagerProvider);
  }

  public static GetAvailableModelsUseCase provideGetAvailableModelsUseCase(
      ProviderManager providerManager) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideGetAvailableModelsUseCase(providerManager));
  }
}
