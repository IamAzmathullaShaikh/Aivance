package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.SettingsRepository;
import com.bangersoul.aivance.core.domain.usecase.provider.SelectProviderUseCase;
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
public final class UseCaseModule_ProvideSelectProviderUseCaseFactory implements Factory<SelectProviderUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private UseCaseModule_ProvideSelectProviderUseCaseFactory(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public SelectProviderUseCase get() {
    return provideSelectProviderUseCase(settingsRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideSelectProviderUseCaseFactory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new UseCaseModule_ProvideSelectProviderUseCaseFactory(settingsRepositoryProvider);
  }

  public static SelectProviderUseCase provideSelectProviderUseCase(
      SettingsRepository settingsRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideSelectProviderUseCase(settingsRepository));
  }
}
