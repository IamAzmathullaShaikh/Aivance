package com.bangersoul.aivance.core.data.repository;

import com.bangersoul.aivance.core.common.model.UserProfile;
import com.bangersoul.aivance.core.data.cache.CacheManager;
import com.bangersoul.aivance.core.data.source.UserLocalDataSource;
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
public final class UserRepositoryImpl_Factory implements Factory<UserRepositoryImpl> {
  private final Provider<UserLocalDataSource> localDataSourceProvider;

  private final Provider<CacheManager<String, UserProfile>> profileCacheProvider;

  private UserRepositoryImpl_Factory(Provider<UserLocalDataSource> localDataSourceProvider,
      Provider<CacheManager<String, UserProfile>> profileCacheProvider) {
    this.localDataSourceProvider = localDataSourceProvider;
    this.profileCacheProvider = profileCacheProvider;
  }

  @Override
  public UserRepositoryImpl get() {
    return newInstance(localDataSourceProvider.get(), profileCacheProvider.get());
  }

  public static UserRepositoryImpl_Factory create(
      Provider<UserLocalDataSource> localDataSourceProvider,
      Provider<CacheManager<String, UserProfile>> profileCacheProvider) {
    return new UserRepositoryImpl_Factory(localDataSourceProvider, profileCacheProvider);
  }

  public static UserRepositoryImpl newInstance(UserLocalDataSource localDataSource,
      CacheManager<String, UserProfile> profileCache) {
    return new UserRepositoryImpl(localDataSource, profileCache);
  }
}
