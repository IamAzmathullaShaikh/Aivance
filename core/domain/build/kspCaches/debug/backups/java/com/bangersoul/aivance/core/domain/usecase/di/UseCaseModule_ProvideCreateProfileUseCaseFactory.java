package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.UserRepository;
import com.bangersoul.aivance.core.domain.usecase.user.CreateProfileUseCase;
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
public final class UseCaseModule_ProvideCreateProfileUseCaseFactory implements Factory<CreateProfileUseCase> {
  private final Provider<UserRepository> userRepositoryProvider;

  private UseCaseModule_ProvideCreateProfileUseCaseFactory(
      Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public CreateProfileUseCase get() {
    return provideCreateProfileUseCase(userRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideCreateProfileUseCaseFactory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new UseCaseModule_ProvideCreateProfileUseCaseFactory(userRepositoryProvider);
  }

  public static CreateProfileUseCase provideCreateProfileUseCase(UserRepository userRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideCreateProfileUseCase(userRepository));
  }
}
