package com.bangersoul.aivance.core.data.repository;

import com.bangersoul.aivance.core.data.source.JobLocalDataSource;
import com.bangersoul.aivance.core.database.dao.JobDao;
import com.bangersoul.aivance.sdk.infrastructure.ProviderManager;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
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
public final class JobRepositoryImpl_Factory implements Factory<JobRepositoryImpl> {
  private final Provider<JobLocalDataSource> localDataSourceProvider;

  private final Provider<JobDao> jobDaoProvider;

  private final Provider<ProviderManager> providerManagerProvider;

  private JobRepositoryImpl_Factory(Provider<JobLocalDataSource> localDataSourceProvider,
      Provider<JobDao> jobDaoProvider, Provider<ProviderManager> providerManagerProvider) {
    this.localDataSourceProvider = localDataSourceProvider;
    this.jobDaoProvider = jobDaoProvider;
    this.providerManagerProvider = providerManagerProvider;
  }

  @Override
  public JobRepositoryImpl get() {
    return newInstance(localDataSourceProvider.get(), jobDaoProvider.get(), providerManagerProvider.get());
  }

  public static JobRepositoryImpl_Factory create(
      Provider<JobLocalDataSource> localDataSourceProvider, Provider<JobDao> jobDaoProvider,
      Provider<ProviderManager> providerManagerProvider) {
    return new JobRepositoryImpl_Factory(localDataSourceProvider, jobDaoProvider, providerManagerProvider);
  }

  public static JobRepositoryImpl newInstance(JobLocalDataSource localDataSource, JobDao jobDao,
      ProviderManager providerManager) {
    return new JobRepositoryImpl(localDataSource, jobDao, providerManager);
  }
}
