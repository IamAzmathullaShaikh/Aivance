package com.bangersoul.aivance.core.data.repository;

import com.bangersoul.aivance.core.common.model.AiProviderConfig;
import com.bangersoul.aivance.core.data.cache.CacheManager;
import com.bangersoul.aivance.core.data.source.AiLocalDataSource;
import com.bangersoul.aivance.core.data.source.SettingsLocalDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.util.List;
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
public final class SettingsRepositoryImpl_Factory implements Factory<SettingsRepositoryImpl> {
  private final Provider<SettingsLocalDataSource> settingsLocalDataSourceProvider;

  private final Provider<AiLocalDataSource> aiLocalDataSourceProvider;

  private final Provider<CacheManager<String, List<AiProviderConfig>>> configCacheProvider;

  private SettingsRepositoryImpl_Factory(
      Provider<SettingsLocalDataSource> settingsLocalDataSourceProvider,
      Provider<AiLocalDataSource> aiLocalDataSourceProvider,
      Provider<CacheManager<String, List<AiProviderConfig>>> configCacheProvider) {
    this.settingsLocalDataSourceProvider = settingsLocalDataSourceProvider;
    this.aiLocalDataSourceProvider = aiLocalDataSourceProvider;
    this.configCacheProvider = configCacheProvider;
  }

  @Override
  public SettingsRepositoryImpl get() {
    return newInstance(settingsLocalDataSourceProvider.get(), aiLocalDataSourceProvider.get(), configCacheProvider.get());
  }

  public static SettingsRepositoryImpl_Factory create(
      Provider<SettingsLocalDataSource> settingsLocalDataSourceProvider,
      Provider<AiLocalDataSource> aiLocalDataSourceProvider,
      Provider<CacheManager<String, List<AiProviderConfig>>> configCacheProvider) {
    return new SettingsRepositoryImpl_Factory(settingsLocalDataSourceProvider, aiLocalDataSourceProvider, configCacheProvider);
  }

  public static SettingsRepositoryImpl newInstance(SettingsLocalDataSource settingsLocalDataSource,
      AiLocalDataSource aiLocalDataSource,
      CacheManager<String, List<AiProviderConfig>> configCache) {
    return new SettingsRepositoryImpl(settingsLocalDataSource, aiLocalDataSource, configCache);
  }
}
