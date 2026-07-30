package com.bangersoul.aivance.core.domain.usecase.provider;

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
public final class DisableProviderUseCase_Factory implements Factory<DisableProviderUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private DisableProviderUseCase_Factory(Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public DisableProviderUseCase get() {
    return newInstance(settingsRepositoryProvider.get());
  }

  public static DisableProviderUseCase_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new DisableProviderUseCase_Factory(settingsRepositoryProvider);
  }

  public static DisableProviderUseCase newInstance(SettingsRepository settingsRepository) {
    return new DisableProviderUseCase(settingsRepository);
  }
}
