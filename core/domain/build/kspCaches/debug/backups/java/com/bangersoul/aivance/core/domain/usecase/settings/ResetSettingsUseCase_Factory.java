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
public final class ResetSettingsUseCase_Factory implements Factory<ResetSettingsUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private ResetSettingsUseCase_Factory(Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public ResetSettingsUseCase get() {
    return newInstance(settingsRepositoryProvider.get());
  }

  public static ResetSettingsUseCase_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new ResetSettingsUseCase_Factory(settingsRepositoryProvider);
  }

  public static ResetSettingsUseCase newInstance(SettingsRepository settingsRepository) {
    return new ResetSettingsUseCase(settingsRepository);
  }
}
