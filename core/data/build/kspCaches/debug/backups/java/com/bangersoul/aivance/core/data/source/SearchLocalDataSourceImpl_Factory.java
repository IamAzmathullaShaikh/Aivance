package com.bangersoul.aivance.core.data.source;

import com.bangersoul.aivance.core.database.dao.SearchDao;
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
public final class SearchLocalDataSourceImpl_Factory implements Factory<SearchLocalDataSourceImpl> {
  private final Provider<SearchDao> searchDaoProvider;

  private SearchLocalDataSourceImpl_Factory(Provider<SearchDao> searchDaoProvider) {
    this.searchDaoProvider = searchDaoProvider;
  }

  @Override
  public SearchLocalDataSourceImpl get() {
    return newInstance(searchDaoProvider.get());
  }

  public static SearchLocalDataSourceImpl_Factory create(Provider<SearchDao> searchDaoProvider) {
    return new SearchLocalDataSourceImpl_Factory(searchDaoProvider);
  }

  public static SearchLocalDataSourceImpl newInstance(SearchDao searchDao) {
    return new SearchLocalDataSourceImpl(searchDao);
  }
}
