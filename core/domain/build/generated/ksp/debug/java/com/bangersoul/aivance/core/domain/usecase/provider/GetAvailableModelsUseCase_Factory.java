package com.bangersoul.aivance.core.domain.usecase.provider;

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
public final class GetAvailableModelsUseCase_Factory implements Factory<GetAvailableModelsUseCase> {
  private final Provider<ProviderManager> providerManagerProvider;

  private GetAvailableModelsUseCase_Factory(Provider<ProviderManager> providerManagerProvider) {
    this.providerManagerProvider = providerManagerProvider;
  }

  @Override
  public GetAvailableModelsUseCase get() {
    return newInstance(providerManagerProvider.get());
  }

  public static GetAvailableModelsUseCase_Factory create(
      Provider<ProviderManager> providerManagerProvider) {
    return new GetAvailableModelsUseCase_Factory(providerManagerProvider);
  }

  public static GetAvailableModelsUseCase newInstance(ProviderManager providerManager) {
    return new GetAvailableModelsUseCase(providerManager);
  }
}
