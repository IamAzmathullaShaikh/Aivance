package com.bangersoul.aivance.core.data.di;

import com.bangersoul.aivance.core.common.model.AiProviderConfig;
import com.bangersoul.aivance.core.data.cache.CacheManager;
import com.bangersoul.aivance.core.data.util.Clock;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import java.util.List;
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
public final class DataModule_Companion_ProvideAiConfigCacheFactory implements Factory<CacheManager<String, List<AiProviderConfig>>> {
  private final Provider<Clock> clockProvider;

  private DataModule_Companion_ProvideAiConfigCacheFactory(Provider<Clock> clockProvider) {
    this.clockProvider = clockProvider;
  }

  @Override
  public CacheManager<String, List<AiProviderConfig>> get() {
    return provideAiConfigCache(clockProvider.get());
  }

  public static DataModule_Companion_ProvideAiConfigCacheFactory create(
      Provider<Clock> clockProvider) {
    return new DataModule_Companion_ProvideAiConfigCacheFactory(clockProvider);
  }

  public static CacheManager<String, List<AiProviderConfig>> provideAiConfigCache(Clock clock) {
    return Preconditions.checkNotNullFromProvides(DataModule.Companion.provideAiConfigCache(clock));
  }
}
