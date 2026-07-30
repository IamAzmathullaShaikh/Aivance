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
public final class SaveSettingsUseCase_Factory implements Factory<SaveSettingsUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private SaveSettingsUseCase_Factory(Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public SaveSettingsUseCase get() {
    return newInstance(settingsRepositoryProvider.get());
  }

  public static SaveSettingsUseCase_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new SaveSettingsUseCase_Factory(settingsRepositoryProvider);
  }

  public static SaveSettingsUseCase newInstance(SettingsRepository settingsRepository) {
    return new SaveSettingsUseCase(settingsRepository);
  }
}
