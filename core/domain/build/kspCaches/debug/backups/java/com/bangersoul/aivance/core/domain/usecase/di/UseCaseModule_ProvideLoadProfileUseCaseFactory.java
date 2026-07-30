package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.UserRepository;
import com.bangersoul.aivance.core.domain.usecase.user.LoadProfileUseCase;
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
public final class UseCaseModule_ProvideLoadProfileUseCaseFactory implements Factory<LoadProfileUseCase> {
  private final Provider<UserRepository> userRepositoryProvider;

  private UseCaseModule_ProvideLoadProfileUseCaseFactory(
      Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public LoadProfileUseCase get() {
    return provideLoadProfileUseCase(userRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideLoadProfileUseCaseFactory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new UseCaseModule_ProvideLoadProfileUseCaseFactory(userRepositoryProvider);
  }

  public static LoadProfileUseCase provideLoadProfileUseCase(UserRepository userRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideLoadProfileUseCase(userRepository));
  }
}
