package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.SettingsRepository;
import com.bangersoul.aivance.core.domain.usecase.settings.ResetSettingsUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
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
public final class UseCaseModule_ProvideResetSettingsUseCaseFactory implements Factory<ResetSettingsUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private UseCaseModule_ProvideResetSettingsUseCaseFactory(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public ResetSettingsUseCase get() {
    return provideResetSettingsUseCase(settingsRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideResetSettingsUseCaseFactory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new UseCaseModule_ProvideResetSettingsUseCaseFactory(settingsRepositoryProvider);
  }

  public static ResetSettingsUseCase provideResetSettingsUseCase(
      SettingsRepository settingsRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideResetSettingsUseCase(settingsRepository));
  }
}
