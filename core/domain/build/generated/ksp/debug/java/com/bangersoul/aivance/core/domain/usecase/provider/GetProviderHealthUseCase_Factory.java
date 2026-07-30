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
public final class GetProviderHealthUseCase_Factory implements Factory<GetProviderHealthUseCase> {
  private final Provider<ProviderManager> providerManagerProvider;

  private GetProviderHealthUseCase_Factory(Provider<ProviderManager> providerManagerProvider) {
    this.providerManagerProvider = providerManagerProvider;
  }

  @Override
  public GetProviderHealthUseCase get() {
    return newInstance(providerManagerProvider.get());
  }

  public static GetProviderHealthUseCase_Factory create(
      Provider<ProviderManager> providerManagerProvider) {
    return new GetProviderHealthUseCase_Factory(providerManagerProvider);
  }

  public static GetProviderHealthUseCase newInstance(ProviderManager providerManager) {
    return new GetProviderHealthUseCase(providerManager);
  }
}
