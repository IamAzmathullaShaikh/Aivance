package com.bangersoul.aivance.ai.di;

import android.content.Context;
import com.bangersoul.aivance.sdk.api.AIProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AiProvidersModule_ProvideGeminiProviderFactory implements Factory<AIProvider> {
  private final Provider<Context> contextProvider;

  private AiProvidersModule_ProvideGeminiProviderFactory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AIProvider get() {
    return provideGeminiProvider(contextProvider.get());
  }

  public static AiProvidersModule_ProvideGeminiProviderFactory create(
      Provider<Context> contextProvider) {
    return new AiProvidersModule_ProvideGeminiProviderFactory(contextProvider);
  }

  public static AIProvider provideGeminiProvider(Context context) {
    return Preconditions.checkNotNullFromProvides(AiProvidersModule.INSTANCE.provideGeminiProvider(context));
  }
}
