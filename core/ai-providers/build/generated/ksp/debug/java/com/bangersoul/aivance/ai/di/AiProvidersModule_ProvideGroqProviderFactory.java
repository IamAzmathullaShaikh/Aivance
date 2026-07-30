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
public final class AiProvidersModule_ProvideGroqProviderFactory implements Factory<AIProvider> {
  @Override
  public AIProvider get() {
    return provideGroqProvider();
  }

  public static AiProvidersModule_ProvideGroqProviderFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AIProvider provideGroqProvider() {
    return Preconditions.checkNotNullFromProvides(AiProvidersModule.INSTANCE.provideGroqProvider());
  }

  private static final class InstanceHolder {
    static final AiProvidersModule_ProvideGroqProviderFactory INSTANCE = new AiProvidersModule_ProvideGroqProviderFactory();
  }
}
