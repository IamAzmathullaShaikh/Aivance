package com.bangersoul.aivance.job.di;

import com.bangersoul.aivance.job.cache.JobCache;
import com.bangersoul.aivance.sdk.api.JobProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class JobProvidersModule_Companion_ProvideRemoteOKProviderFactory implements Factory<JobProvider> {
  private final Provider<JobCache> jobCacheProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<Retrofit> retrofitProvider;

  private JobProvidersModule_Companion_ProvideRemoteOKProviderFactory(
      Provider<JobCache> jobCacheProvider, Provider<OkHttpClient> okHttpClientProvider,
      Provider<Retrofit> retrofitProvider) {
    this.jobCacheProvider = jobCacheProvider;
    this.okHttpClientProvider = okHttpClientProvider;
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public JobProvider get() {
    return provideRemoteOKProvider(jobCacheProvider.get(), okHttpClientProvider.get(), retrofitProvider.get());
  }

  public static JobProvidersModule_Companion_ProvideRemoteOKProviderFactory create(
      Provider<JobCache> jobCacheProvider, Provider<OkHttpClient> okHttpClientProvider,
      Provider<Retrofit> retrofitProvider) {
    return new JobProvidersModule_Companion_ProvideRemoteOKProviderFactory(jobCacheProvider, okHttpClientProvider, retrofitProvider);
  }

  public static JobProvider provideRemoteOKProvider(JobCache jobCache, OkHttpClient okHttpClient,
      Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(JobProvidersModule.Companion.provideRemoteOKProvider(jobCache, okHttpClient, retrofit));
  }
}
