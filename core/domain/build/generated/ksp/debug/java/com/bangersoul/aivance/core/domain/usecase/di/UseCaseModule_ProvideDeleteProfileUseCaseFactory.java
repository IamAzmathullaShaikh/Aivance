package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.UserRepository;
import com.bangersoul.aivance.core.domain.usecase.user.DeleteProfileUseCase;
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
public final class UseCaseModule_ProvideDeleteProfileUseCaseFactory implements Factory<DeleteProfileUseCase> {
  private final Provider<UserRepository> userRepositoryProvider;

  private UseCaseModule_ProvideDeleteProfileUseCaseFactory(
      Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public DeleteProfileUseCase get() {
    return provideDeleteProfileUseCase(userRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideDeleteProfileUseCaseFactory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new UseCaseModule_ProvideDeleteProfileUseCaseFactory(userRepositoryProvider);
  }

  public static DeleteProfileUseCase provideDeleteProfileUseCase(UserRepository userRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideDeleteProfileUseCase(userRepository));
  }
}
