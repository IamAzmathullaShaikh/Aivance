package com.bangersoul.aivance.ai.di;

import android.content.Context;
import com.bangersoul.aivance.sdk.infrastructure.ProviderFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class AiProvidersModule_ProvideGeminiFactoryFactory implements Factory<ProviderFactory.Factory> {
  private final Provider<Context> contextProvider;

  private AiProvidersModule_ProvideGeminiFactoryFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ProviderFactory.Factory get() {
    return provideGeminiFactory(contextProvider.get());
  }

  public static AiProvidersModule_ProvideGeminiFactoryFactory create(
      Provider<Context> contextProvider) {
    return new AiProvidersModule_ProvideGeminiFactoryFactory(contextProvider);
  }

  public static ProviderFactory.Factory provideGeminiFactory(Context context) {
    return Preconditions.checkNotNullFromProvides(AiProvidersModule.INSTANCE.provideGeminiFactory(context));
  }
}
