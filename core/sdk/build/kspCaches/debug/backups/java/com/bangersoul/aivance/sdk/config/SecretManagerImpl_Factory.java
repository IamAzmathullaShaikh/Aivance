package com.bangersoul.aivance.sdk.config;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SecretManagerImpl_Factory implements Factory<SecretManagerImpl> {
  private final Provider<Context> contextProvider;

  private SecretManagerImpl_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SecretManagerImpl get() {
    return newInstance(contextProvider.get());
  }

  public static SecretManagerImpl_Factory create(Provider<Context> contextProvider) {
    return new SecretManagerImpl_Factory(contextProvider);
  }

  public static SecretManagerImpl newInstance(Context context) {
    return new SecretManagerImpl(context);
  }
}
