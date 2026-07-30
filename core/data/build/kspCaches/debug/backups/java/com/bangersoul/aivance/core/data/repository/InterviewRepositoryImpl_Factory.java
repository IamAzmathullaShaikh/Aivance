package com.bangersoul.aivance.core.data.repository;

import com.bangersoul.aivance.core.data.source.InterviewLocalDataSource;
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
public final class InterviewRepositoryImpl_Factory implements Factory<InterviewRepositoryImpl> {
  private final Provider<InterviewLocalDataSource> localDataSourceProvider;

  private final Provider<ProviderManager> providerManagerProvider;

  private InterviewRepositoryImpl_Factory(
      Provider<InterviewLocalDataSource> localDataSourceProvider,
      Provider<ProviderManager> providerManagerProvider) {
    this.localDataSourceProvider = localDataSourceProvider;
    this.providerManagerProvider = providerManagerProvider;
  }

  @Override
  public InterviewRepositoryImpl get() {
    return newInstance(localDataSourceProvider.get(), providerManagerProvider.get());
  }

  public static InterviewRepositoryImpl_Factory create(
      Provider<InterviewLocalDataSource> localDataSourceProvider,
      Provider<ProviderManager> providerManagerProvider) {
    return new InterviewRepositoryImpl_Factory(localDataSourceProvider, providerManagerProvider);
  }

  public static InterviewRepositoryImpl newInstance(InterviewLocalDataSource localDataSource,
      ProviderManager providerManager) {
    return new InterviewRepositoryImpl(localDataSource, providerManager);
  }
}
