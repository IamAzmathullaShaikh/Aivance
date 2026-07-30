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
public final class CreateProfileUseCase_Factory implements Factory<CreateProfileUseCase> {
  private final Provider<UserRepository> userRepositoryProvider;

  private CreateProfileUseCase_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public CreateProfileUseCase get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static CreateProfileUseCase_Factory create(
      Provider<UserRepository> userRepositoryProvider) {
    return new CreateProfileUseCase_Factory(userRepositoryProvider);
  }

  public static CreateProfileUseCase newInstance(UserRepository userRepository) {
    return new CreateProfileUseCase(userRepository);
  }
}
