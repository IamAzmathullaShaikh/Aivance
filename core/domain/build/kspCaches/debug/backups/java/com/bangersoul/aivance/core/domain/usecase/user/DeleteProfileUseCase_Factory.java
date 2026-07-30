package com.bangersoul.aivance.core.domain.usecase.user;

import com.bangersoul.aivance.core.domain.repository.UserRepository;
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
public final class DeleteProfileUseCase_Factory implements Factory<DeleteProfileUseCase> {
  private final Provider<UserRepository> userRepositoryProvider;

  private DeleteProfileUseCase_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public DeleteProfileUseCase get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static DeleteProfileUseCase_Factory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new DeleteProfileUseCase_Factory(userRepositoryProvider);
  }

  public static DeleteProfileUseCase newInstance(UserRepository userRepository) {
    return new DeleteProfileUseCase(userRepository);
  }
}
