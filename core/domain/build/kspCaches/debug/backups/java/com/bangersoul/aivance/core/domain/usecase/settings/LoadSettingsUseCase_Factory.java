package com.bangersoul.aivance.core.domain.usecase.settings;

import com.bangersoul.aivance.core.domain.repository.SettingsRepository;
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
public final class LoadSettingsUseCase_Factory implements Factory<LoadSettingsUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private LoadSettingsUseCase_Factory(Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public LoadSettingsUseCase get() {
    return newInstance(settingsRepositoryProvider.get());
  }

  public static LoadSettingsUseCase_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new LoadSettingsUseCase_Factory(settingsRepositoryProvider);
  }

  public static LoadSettingsUseCase newInstance(SettingsRepository settingsRepository) {
    return new LoadSettingsUseCase(settingsRepository);
  }
}
