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
public final class SelectProviderUseCase_Factory implements Factory<SelectProviderUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private SelectProviderUseCase_Factory(Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public SelectProviderUseCase get() {
    return newInstance(settingsRepositoryProvider.get());
  }

  public static SelectProviderUseCase_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new SelectProviderUseCase_Factory(settingsRepositoryProvider);
  }

  public static SelectProviderUseCase newInstance(SettingsRepository settingsRepository) {
    return new SelectProviderUseCase(settingsRepository);
  }
}
