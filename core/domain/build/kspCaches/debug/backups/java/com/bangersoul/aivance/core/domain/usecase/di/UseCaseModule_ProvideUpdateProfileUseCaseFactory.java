package com.bangersoul.aivance.core.domain.usecase.di;

import com.bangersoul.aivance.core.domain.repository.UserRepository;
import com.bangersoul.aivance.core.domain.usecase.user.UpdateProfileUseCase;
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
public final class UseCaseModule_ProvideUpdateProfileUseCaseFactory implements Factory<UpdateProfileUseCase> {
  private final Provider<UserRepository> userRepositoryProvider;

  private UseCaseModule_ProvideUpdateProfileUseCaseFactory(
      Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public UpdateProfileUseCase get() {
    return provideUpdateProfileUseCase(userRepositoryProvider.get());
  }

  public static UseCaseModule_ProvideUpdateProfileUseCaseFactory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new UseCaseModule_ProvideUpdateProfileUseCaseFactory(userRepositoryProvider);
  }

  public static UpdateProfileUseCase provideUpdateProfileUseCase(UserRepository userRepository) {
    return Preconditions.checkNotNullFromProvides(UseCaseModule.INSTANCE.provideUpdateProfileUseCase(userRepository));
  }
}
