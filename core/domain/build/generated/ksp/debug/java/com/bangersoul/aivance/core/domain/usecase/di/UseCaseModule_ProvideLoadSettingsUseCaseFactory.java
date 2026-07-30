package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.SettingsRepository;
import com.bangersoul.aivance.core.domain.usecase.settings.LoadSettingsUseCase;
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
public final class UseCaseModule_ProvideLoadSettingsUseCaseFactory implements Factory<LoadSettingsUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private UseCaseModule_ProvideLoadSettingsUseCaseFactory(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public LoadSettingsUseCase get() {
    return provideLoadSettingsUseCase(settingsRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideLoadSettingsUseCaseFactory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new UseCaseModule_ProvideLoadSettingsUseCaseFactory(settingsRepositoryProvider);
  }

  public static LoadSettingsUseCase provideLoadSettingsUseCase(
      SettingsRepository settingsRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideLoadSettingsUseCase(settingsRepository));
  }
}
