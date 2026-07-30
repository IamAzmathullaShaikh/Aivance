package com.bangersoul.aivance.ai.di;

import com.bangersoul.aivance.sdk.api.AIProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AiProvidersModule_ProvideOpenRouterProviderFactory implements Factory<AIProvider> {
  @Override
  public AIProvider get() {
    return provideOpenRouterProvider();
  }

  public static AiProvidersModule_ProvideOpenRouterProviderFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AIProvider provideOpenRouterProvider() {
    return Preconditions.checkNotNullFromProvides(AiProvidersModule.INSTANCE.provideOpenRouterProvider());
  }

  private static final class InstanceHolder {
    static final AiProvidersModule_ProvideOpenRouterProviderFactory INSTANCE = new AiProvidersModule_ProvideOpenRouterProviderFactory();
  }
}
