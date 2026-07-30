package com.bangersoul.aivance.job.cache;

import com.bangersoul.aivance.core.common.model.JobListing;
import com.bangersoul.aivance.core.data.cache.CacheManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class MemoryJobCache_Factory implements Factory<MemoryJobCache> {
  private final Provider<CacheManager<String, List<JobListing>>> cacheManagerProvider;

  private MemoryJobCache_Factory(
      Provider<CacheManager<String, List<JobListing>>> cacheManagerProvider) {
    this.cacheManagerProvider = cacheManagerProvider;
  }

  @Override
  public MemoryJobCache get() {
    return newInstance(cacheManagerProvider.get());
  }

  public static MemoryJobCache_Factory create(
      Provider<CacheManager<String, List<JobListing>>> cacheManagerProvider) {
    return new MemoryJobCache_Factory(cacheManagerProvider);
  }

  public static MemoryJobCache newInstance(CacheManager<String, List<JobListing>> cacheManager) {
    return new MemoryJobCache(cacheManager);
  }
}
