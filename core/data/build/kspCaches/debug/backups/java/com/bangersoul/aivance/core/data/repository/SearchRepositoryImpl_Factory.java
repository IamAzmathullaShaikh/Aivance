package com.bangersoul.aivance.core.data.repository;

import com.bangersoul.aivance.core.data.source.SearchLocalDataSource;
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
public final class SearchRepositoryImpl_Factory implements Factory<SearchRepositoryImpl> {
  private final Provider<SearchLocalDataSource> localDataSourceProvider;

  private SearchRepositoryImpl_Factory(Provider<SearchLocalDataSource> localDataSourceProvider) {
    this.localDataSourceProvider = localDataSourceProvider;
  }

  @Override
  public SearchRepositoryImpl get() {
    return newInstance(localDataSourceProvider.get());
  }

  public static SearchRepositoryImpl_Factory create(
      Provider<SearchLocalDataSource> localDataSourceProvider) {
    return new SearchRepositoryImpl_Factory(localDataSourceProvider);
  }

  public static SearchRepositoryImpl newInstance(SearchLocalDataSource localDataSource) {
    return new SearchRepositoryImpl(localDataSource);
  }
}
