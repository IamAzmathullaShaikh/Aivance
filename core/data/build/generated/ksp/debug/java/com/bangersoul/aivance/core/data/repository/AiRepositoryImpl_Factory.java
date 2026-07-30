package com.bangersoul.aivance.core.data.repository;

import com.bangersoul.aivance.core.data.source.AiLocalDataSource;
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
public final class AiRepositoryImpl_Factory implements Factory<AiRepositoryImpl> {
  private final Provider<AiLocalDataSource> localDataSourceProvider;

  private final Provider<ProviderManager> providerManagerProvider;

  private AiRepositoryImpl_Factory(Provider<AiLocalDataSource> localDataSourceProvider,
      Provider<ProviderManager> providerManagerProvider) {
    this.localDataSourceProvider = localDataSourceProvider;
    this.providerManagerProvider = providerManagerProvider;
  }

  @Override
  public AiRepositoryImpl get() {
    return newInstance(localDataSourceProvider.get(), providerManagerProvider.get());
  }

  public static AiRepositoryImpl_Factory create(Provider<AiLocalDataSource> localDataSourceProvider,
      Provider<ProviderManager> providerManagerProvider) {
    return new AiRepositoryImpl_Factory(localDataSourceProvider, providerManagerProvider);
  }

  public static AiRepositoryImpl newInstance(AiLocalDataSource localDataSource,
      ProviderManager providerManager) {
    return new AiRepositoryImpl(localDataSource, providerManager);
  }
}
