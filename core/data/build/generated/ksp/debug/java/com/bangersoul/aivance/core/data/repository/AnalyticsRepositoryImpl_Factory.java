package com.bangersoul.aivance.core.data.repository;

import com.bangersoul.aivance.core.data.source.AiLocalDataSource;
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
public final class AnalyticsRepositoryImpl_Factory implements Factory<AnalyticsRepositoryImpl> {
  private final Provider<AiLocalDataSource> localDataSourceProvider;

  private AnalyticsRepositoryImpl_Factory(Provider<AiLocalDataSource> localDataSourceProvider) {
    this.localDataSourceProvider = localDataSourceProvider;
  }

  @Override
  public AnalyticsRepositoryImpl get() {
    return newInstance(localDataSourceProvider.get());
  }

  public static AnalyticsRepositoryImpl_Factory create(
      Provider<AiLocalDataSource> localDataSourceProvider) {
    return new AnalyticsRepositoryImpl_Factory(localDataSourceProvider);
  }

  public static AnalyticsRepositoryImpl newInstance(AiLocalDataSource localDataSource) {
    return new AnalyticsRepositoryImpl(localDataSource);
  }
}
