package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.SettingsRepository;
import com.bangersoul.aivance.core.domain.usecase.provider.EnableProviderUseCase;
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
public final class UseCaseModule_ProvideEnableProviderUseCaseFactory implements Factory<EnableProviderUseCase> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private UseCaseModule_ProvideEnableProviderUseCaseFactory(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public EnableProviderUseCase get() {
    return provideEnableProviderUseCase(settingsRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideEnableProviderUseCaseFactory create(
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new UseCaseModule_ProvideEnableProviderUseCaseFactory(settingsRepositoryProvider);
  }

  public static EnableProviderUseCase provideEnableProviderUseCase(
      SettingsRepository settingsRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideEnableProviderUseCase(settingsRepository));
  }
}
