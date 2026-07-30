package com.bangersoul.aivance.core.data.repository;

import com.bangersoul.aivance.core.data.source.CoverLetterLocalDataSource;
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
public final class CoverLetterRepositoryImpl_Factory implements Factory<CoverLetterRepositoryImpl> {
  private final Provider<CoverLetterLocalDataSource> localDataSourceProvider;

  private final Provider<ProviderManager> providerManagerProvider;

  private CoverLetterRepositoryImpl_Factory(
      Provider<CoverLetterLocalDataSource> localDataSourceProvider,
      Provider<ProviderManager> providerManagerProvider) {
    this.localDataSourceProvider = localDataSourceProvider;
    this.providerManagerProvider = providerManagerProvider;
  }

  @Override
  public CoverLetterRepositoryImpl get() {
    return newInstance(localDataSourceProvider.get(), providerManagerProvider.get());
  }

  public static CoverLetterRepositoryImpl_Factory create(
      Provider<CoverLetterLocalDataSource> localDataSourceProvider,
      Provider<ProviderManager> providerManagerProvider) {
    return new CoverLetterRepositoryImpl_Factory(localDataSourceProvider, providerManagerProvider);
  }

  public static CoverLetterRepositoryImpl newInstance(CoverLetterLocalDataSource localDataSource,
      ProviderManager providerManager) {
    return new CoverLetterRepositoryImpl(localDataSource, providerManager);
  }
}
