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
public final class EnableProviderUseCase_Factory implements Factory<EnableProviderUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private EnableProviderUseCase_Factory(Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public EnableProviderUseCase get() {
    return newInstance(settingsRepositoryProvider.get());
  }

  public static EnableProviderUseCase_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new EnableProviderUseCase_Factory(settingsRepositoryProvider);
  }

  public static EnableProviderUseCase newInstance(SettingsRepository settingsRepository) {
    return new EnableProviderUseCase(settingsRepository);
  }
}
