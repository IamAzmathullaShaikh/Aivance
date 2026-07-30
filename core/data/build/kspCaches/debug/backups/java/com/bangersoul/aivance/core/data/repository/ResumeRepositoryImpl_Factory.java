package com.bangersoul.aivance.core.data.repository;

import com.bangersoul.aivance.core.data.source.ResumeLocalDataSource;
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
public final class ResumeRepositoryImpl_Factory implements Factory<ResumeRepositoryImpl> {
  private final Provider<ResumeLocalDataSource> localDataSourceProvider;

  private final Provider<ProviderManager> providerManagerProvider;

  private ResumeRepositoryImpl_Factory(Provider<ResumeLocalDataSource> localDataSourceProvider,
      Provider<ProviderManager> providerManagerProvider) {
    this.localDataSourceProvider = localDataSourceProvider;
    this.providerManagerProvider = providerManagerProvider;
  }

  @Override
  public ResumeRepositoryImpl get() {
    return newInstance(localDataSourceProvider.get(), providerManagerProvider.get());
  }

  public static ResumeRepositoryImpl_Factory create(
      Provider<ResumeLocalDataSource> localDataSourceProvider,
      Provider<ProviderManager> providerManagerProvider) {
    return new ResumeRepositoryImpl_Factory(localDataSourceProvider, providerManagerProvider);
  }

  public static ResumeRepositoryImpl newInstance(ResumeLocalDataSource localDataSource,
      ProviderManager providerManager) {
    return new ResumeRepositoryImpl(localDataSource, providerManager);
  }
}
