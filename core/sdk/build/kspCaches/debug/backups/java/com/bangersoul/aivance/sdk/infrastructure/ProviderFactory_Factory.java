package com.bangersoul.aivance.sdk.infrastructure;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.util.Map;
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
public final class ProviderFactory_Factory implements Factory<ProviderFactory> {
  private final Provider<Map<String, ProviderFactory.Factory>> injectedFactoriesProvider;

  private ProviderFactory_Factory(
      Provider<Map<String, ProviderFactory.Factory>> injectedFactoriesProvider) {
    this.injectedFactoriesProvider = injectedFactoriesProvider;
  }

  @Override
  public ProviderFactory get() {
    return newInstance(injectedFactoriesProvider.get());
  }

  public static ProviderFactory_Factory create(
      Provider<Map<String, ProviderFactory.Factory>> injectedFactoriesProvider) {
    return new ProviderFactory_Factory(injectedFactoriesProvider);
  }

  public static ProviderFactory newInstance(
      Map<String, ProviderFactory.Factory> injectedFactories) {
    return new ProviderFactory(injectedFactories);
  }
}
