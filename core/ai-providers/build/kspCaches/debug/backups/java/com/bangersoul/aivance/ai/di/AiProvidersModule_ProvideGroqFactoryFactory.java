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
public final class AiProvidersModule_ProvideGroqFactoryFactory implements Factory<ProviderFactory.Factory> {
  @Override
  public ProviderFactory.Factory get() {
    return provideGroqFactory();
  }

  public static AiProvidersModule_ProvideGroqFactoryFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static ProviderFactory.Factory provideGroqFactory() {
    return Preconditions.checkNotNullFromProvides(AiProvidersModule.INSTANCE.provideGroqFactory());
  }

  private static final class InstanceHolder {
    static final AiProvidersModule_ProvideGroqFactoryFactory INSTANCE = new AiProvidersModule_ProvideGroqFactoryFactory();
  }
}
