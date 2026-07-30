package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.usecase.provider.GetProviderHealthUseCase;
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
public final class UseCaseModule_ProvideGetProviderHealthUseCaseFactory implements Factory<GetProviderHealthUseCase> {
  private final Provider<ProviderManager> providerManagerProvider;

  private UseCaseModule_ProvideGetProviderHealthUseCaseFactory(
      Provider<ProviderManager> providerManagerProvider) {
    this.providerManagerProvider = providerManagerProvider;
  }

  @Override
  public GetProviderHealthUseCase get() {
    return provideGetProviderHealthUseCase(providerManagerProvider.get());
  }

  public static UseCaseModule_ProvideGetProviderHealthUseCaseFactory create(
      Provider<ProviderManager> providerManagerProvider) {
    return new UseCaseModule_ProvideGetProviderHealthUseCaseFactory(providerManagerProvider);
  }

  public static GetProviderHealthUseCase provideGetProviderHealthUseCase(
      ProviderManager providerManager) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideGetProviderHealthUseCase(providerManager));
  }
}
