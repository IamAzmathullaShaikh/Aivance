package com.bangersoul.aivance.sdk.infrastructure;

import com.bangersoul.aivance.sdk.api.AIProvider;
import com.bangersoul.aivance.sdk.api.JobProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.util.Set;
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
public final class ProviderRegistry_Factory implements Factory<ProviderRegistry> {
  private final Provider<Set<AIProvider>> aiProvidersProvider;

  private final Provider<Set<JobProvider>> jobProvidersProvider;

  private ProviderRegistry_Factory(Provider<Set<AIProvider>> aiProvidersProvider,
      Provider<Set<JobProvider>> jobProvidersProvider) {
    this.aiProvidersProvider = aiProvidersProvider;
    this.jobProvidersProvider = jobProvidersProvider;
  }

  @Override
  public ProviderRegistry get() {
    return newInstance(aiProvidersProvider.get(), jobProvidersProvider.get());
  }

  public static ProviderRegistry_Factory create(Provider<Set<AIProvider>> aiProvidersProvider,
      Provider<Set<JobProvider>> jobProvidersProvider) {
    return new ProviderRegistry_Factory(aiProvidersProvider, jobProvidersProvider);
  }

  public static ProviderRegistry newInstance(Set<AIProvider> aiProviders,
      Set<JobProvider> jobProviders) {
    return new ProviderRegistry(aiProviders, jobProviders);
  }
}
