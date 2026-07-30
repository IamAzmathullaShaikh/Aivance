package com.bangersoul.aivance.sdk.infrastructure;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ProviderManager_Factory implements Factory<ProviderManager> {
  private final Provider<ProviderRegistry> registryProvider;

  private ProviderManager_Factory(Provider<ProviderRegistry> registryProvider) {
    this.registryProvider = registryProvider;
  }

  @Override
  public ProviderManager get() {
    return newInstance(registryProvider.get());
  }

  public static ProviderManager_Factory create(Provider<ProviderRegistry> registryProvider) {
    return new ProviderManager_Factory(registryProvider);
  }

  public static ProviderManager newInstance(ProviderRegistry registry) {
    return new ProviderManager(registry);
  }
}
