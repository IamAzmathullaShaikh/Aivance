package com.bangersoul.aivance.ai.di;

import com.bangersoul.aivance.sdk.infrastructure.ProviderFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AiProvidersModule_ProvideOpenRouterFactoryFactory implements Factory<ProviderFactory.Factory> {
  @Override
  public ProviderFactory.Factory get() {
    return provideOpenRouterFactory();
  }

  public static AiProvidersModule_ProvideOpenRouterFactoryFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ProviderFactory.Factory provideOpenRouterFactory() {
    return Preconditions.checkNotNullFromProvides(AiProvidersModule.INSTANCE.provideOpenRouterFactory());
  }

  private static final class InstanceHolder {
    static final AiProvidersModule_ProvideOpenRouterFactoryFactory INSTANCE = new AiProvidersModule_ProvideOpenRouterFactoryFactory();
  }
}
