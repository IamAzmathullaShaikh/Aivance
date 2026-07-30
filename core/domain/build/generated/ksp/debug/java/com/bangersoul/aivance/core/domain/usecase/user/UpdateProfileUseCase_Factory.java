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
public final class UpdateProfileUseCase_Factory implements Factory<UpdateProfileUseCase> {
  private final Provider<UserRepository> userRepositoryProvider;

  private UpdateProfileUseCase_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public UpdateProfileUseCase get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static UpdateProfileUseCase_Factory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new UpdateProfileUseCase_Factory(userRepositoryProvider);
  }

  public static UpdateProfileUseCase newInstance(UserRepository userRepository) {
    return new UpdateProfileUseCase(userRepository);
  }
}
