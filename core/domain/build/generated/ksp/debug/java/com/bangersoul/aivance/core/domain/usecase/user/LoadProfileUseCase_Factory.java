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
public final class LoadProfileUseCase_Factory implements Factory<LoadProfileUseCase> {
  private final Provider<UserRepository> userRepositoryProvider;

  private LoadProfileUseCase_Factory(Provider<UserRepository> userRepositoryProvider) {
    this.userRepositoryProvider = userRepositoryProvider;
  }

  @Override
  public LoadProfileUseCase get() {
    return newInstance(userRepositoryProvider.get());
  }

  public static LoadProfileUseCase_Factory create(Provider<UserRepository> userRepositoryProvider) {
    return new LoadProfileUseCase_Factory(userRepositoryProvider);
  }

  public static LoadProfileUseCase newInstance(UserRepository userRepository) {
    return new LoadProfileUseCase(userRepository);
  }
}
