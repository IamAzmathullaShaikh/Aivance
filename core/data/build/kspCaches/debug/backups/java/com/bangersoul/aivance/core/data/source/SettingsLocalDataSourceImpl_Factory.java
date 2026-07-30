package com.bangersoul.aivance.core.data.source;

import com.bangersoul.aivance.core.datastore.UserPreferencesRepository;
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
public final class SettingsLocalDataSourceImpl_Factory implements Factory<SettingsLocalDataSourceImpl> {
  private final Provider<UserPreferencesRepository> userPreferencesRepositoryProvider;

  private SettingsLocalDataSourceImpl_Factory(
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    this.userPreferencesRepositoryProvider = userPreferencesRepositoryProvider;
  }

  @Override
  public SettingsLocalDataSourceImpl get() {
    return newInstance(userPreferencesRepositoryProvider.get());
  }

  public static SettingsLocalDataSourceImpl_Factory create(
      Provider<UserPreferencesRepository> userPreferencesRepositoryProvider) {
    return new SettingsLocalDataSourceImpl_Factory(userPreferencesRepositoryProvider);
  }

  public static SettingsLocalDataSourceImpl newInstance(
      UserPreferencesRepository userPreferencesRepository) {
    return new SettingsLocalDataSourceImpl(userPreferencesRepository);
  }
}
